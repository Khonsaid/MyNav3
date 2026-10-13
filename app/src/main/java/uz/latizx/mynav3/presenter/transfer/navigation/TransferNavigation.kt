package uz.latizx.mynav3.presenter.transfer.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.actionFilter
import androidx.navigation3.runtime.deeplink.withBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import uz.latizx.mynav3.core.navigation.AppMetadata
import uz.latizx.mynav3.core.navigation.EntryProviderInstaller
import uz.latizx.mynav3.core.navigation.LocalSharedViewModelStoreOwner
import uz.latizx.mynav3.core.navigation.NavTransitions
import uz.latizx.mynav3.core.navigation.Navigator
import uz.latizx.mynav3.core.navigation.SharedViewModelStoreNavEntryDecorator
import uz.latizx.mynav3.core.navigation.deeplink.DeepLinkContributor
import uz.latizx.mynav3.core.navigation.toContentKey
import uz.latizx.mynav3.presenter.common.maskCardNumber
import uz.latizx.mynav3.presenter.transfer.confirm.TransferConfirmDialog
import uz.latizx.mynav3.presenter.transfer.form.TransferFormRoute
import uz.latizx.mynav3.presenter.transfer.form.TransferFormScreenContract
import uz.latizx.mynav3.presenter.transfer.form.TransferFormViewModel
import uz.latizx.mynav3.presenter.transfer.history.TransfersRoute
import uz.latizx.mynav3.presenter.transfer.selectcard.SelectCardRoute
import uz.latizx.mynav3.presenter.transfer.selectcard.SelectCardViewModel
import uz.latizx.mynav3.presenter.transfer.success.TransferSuccessScreen

/**
 * O'TKAZMALAR feature'i — ko'p qadamli oqim (flow) va UMUMIY (shared) VIEWMODEL:
 *
 * ```
 *                ┌──────────── TransferFormViewModel (bitta nusxa) ────────────┐
 *                │                                                             │
 * Tarix ─▶ Forma (PARENT) ─▶ Kartani tanlash (CHILD) ─goBack─▶ Forma           │
 *                │                                                             │
 *                └─▶ Tasdiqlash (CHILD, dialog) ─goBack─▶ Forma ─replaceCurrent─▶ Muvaffaqiyat
 *                                                                              │
 *                        "Tayyor": popUpTo(Tarix) ◀────────────────────────────┤
 *                        "Yana":   replaceCurrent(Forma) ◀─────────────────────┘
 * ```
 *
 * Nega natija (ResultEventBus) emas, umumiy ViewModel? Ekranlar BITTA oqimning qadamlari va bitta
 * holat ustida ishlaydi: karta tanlash shu holatni o'zgartiradi, tasdiqlash shu holatni o'qiydi.
 * Natija bilan bo'lsa — ma'lumot key'larda tashilardi va har bir qadam uchun Result klass kerak
 * bo'lardi. Qoida:
 *  - qayta ishlatiladigan, chaqiruvchini bilmaydigan ekran/dialog → ResultEventBus
 *    (BlockCardDialog, LanguageSheet, LogoutDialog);
 *  - bitta oqimning qadamlari → umumiy ViewModel (shu fayl).
 */
fun EntryProviderScope<NavKey>.transferEntries(navigator: Navigator) {

    entry<TransfersKey>(metadata = AppMetadata.topLevel()) {
        TransfersRoute(
            viewModel = hiltViewModel(),
            onOpenNewTransfer = { navigator.navigate(TransferFormKey()) },
            onOpenRepeatTransfer = { fromCardId, recipient ->
                navigator.navigate(TransferFormKey(fromCardId = fromCardId, recipient = recipient))
            },
        )
    }

    // PARENT: oqimning ViewModel'i shu entry'ning store'ida yashaydi (oddiy hiltViewModel).
    // contentKey'ni o'zimiz belgilaymiz — child entry'lar parent'ni aynan shu kalit bilan topadi.
    entry<TransferFormKey>(clazzContentKey = { key -> key.toContentKey() }) { key ->
        val viewModel = hiltViewModel<TransferFormViewModel, TransferFormViewModel.Factory>(
            creationCallback = { factory -> factory.create(key) },
        )

        TransferFormRoute(
            viewModel = viewModel,
            onNavigateBack = navigator::goBack,
            // Child key'lari parent key'ini (form = key) o'zi bilan olib yuradi
            onOpenSelectCard = { selectedCardId ->
                navigator.navigate(SelectCardKey(form = key, selectedCardId = selectedCardId))
            },
            onOpenConfirm = { navigator.navigate(TransferConfirmKey(form = key)) },
            // replaceCurrent: Muvaffaqiyat ekranidan Back bosilsa forma QAYTA ochilmaydi
            onTransferSuccess = { transferId, amount ->
                navigator.replaceCurrent(TransferSuccessKey(transferId, amount))
            },
        )
    }

    // CHILD (to'liq ekran): ikkita ViewModel —
    //  - O'ZINIKI (SelectCardViewModel): kartalar ro'yxati va belgilangan karta;
    //  - PARENT'NIKI (TransferFormViewModel): tanlov to'g'ridan-to'g'ri oqim holatiga yoziladi.
    // metadata lambda: key'ga bog'liq metadata (parent har safar boshqa bo'lishi mumkin).
    entry<SelectCardKey>(
        metadata = { key -> SharedViewModelStoreNavEntryDecorator.parent(key.form) },
    ) { key ->
        val flowViewModel = sharedTransferFormViewModel(key.form)

        SelectCardRoute(
            viewModel = hiltViewModel<SelectCardViewModel, SelectCardViewModel.Factory>(
                creationCallback = { factory -> factory.create(key) },
            ),
            onCardChosen = { cardId ->
                // Natija klassi ham, ResultEffect ham kerak emas: forma qaytib chizilganda
                // yangi karta allaqachon umumiy holatda turadi.
                flowViewModel.onIntent(TransferFormScreenContract.Intent.CardSelected(cardId))
                navigator.goBack()
            },
            onNavigateBack = navigator::goBack,
        )
    }

    // CHILD (dialog): faqat parent'ning ViewModel'i. Ma'lumotlar key'da tashilmaydi —
    // umumiy holatdan o'qiladi; "Tasdiqlash" ham shu ViewModel'ga Intent sifatida boradi.
    entry<TransferConfirmKey>(
        metadata = { key ->
            DialogSceneStrategy.dialog() + SharedViewModelStoreNavEntryDecorator.parent(key.form)
        },
    ) { key ->
        val flowViewModel = sharedTransferFormViewModel(key.form)
        val state by flowViewModel.uiState.collectAsStateWithLifecycle()

        TransferConfirmDialog(
            fromCardMasked = state.selectedCard?.let { card -> maskCardNumber(card.number) }.orEmpty(),
            recipient = state.recipient,
            amount = state.amountValue,
            onConfirm = {
                // ViewModel o'tkazmani bajaradi → Effect.OpenSuccess → forma (Route) uni
                // Muvaffaqiyat ekraniga aylantiradi. Dialog esa darhol yopiladi.
                flowViewModel.onIntent(TransferFormScreenContract.Intent.TransferConfirmed)
                navigator.goBack()
            },
            onDismiss = navigator::goBack,
        )
    }

    entry<TransferSuccessKey>(metadata = NavTransitions.fade()) { key ->
        TransferSuccessScreen(
            amount = key.amount,
            transferId = key.transferId,
            // popUpTo: tarix ekranigacha qaytish
            onDone = { navigator.popUpTo(TransfersKey) },
            // Muvaffaqiyat ekrani o'rniga yangi bo'sh forma
            onNewTransfer = { navigator.replaceCurrent(TransferFormKey()) },
        )
    }
}

/**
 * Forma (PARENT) entry'si ishlatayotgan AYNAN O'SHA [TransferFormViewModel] nusxasi.
 *
 * `LocalSharedViewModelStoreOwner` — parent'ning ViewModelStore'i (uni decorator beradi, chunki
 * entry metadata'sida `parent(...)` bor). Process death'dan keyin child birinchi chizilsa ham
 * to'g'ri ishlaydi: ViewModel parent key'i ([form]) bilan yaratiladi — xuddi parent yaratgandek.
 */
@Composable
private fun sharedTransferFormViewModel(form: TransferFormKey): TransferFormViewModel =
    hiltViewModel<TransferFormViewModel, TransferFormViewModel.Factory>(
        viewModelStoreOwner = LocalSharedViewModelStoreOwner.current,
        creationCallback = { factory -> factory.create(form) },
    )

@Module
@InstallIn(ActivityRetainedComponent::class)
object TransferNavigationModule {

    @Provides
    @IntoSet
    fun provideTransferEntryInstaller(): EntryProviderInstaller = { navigator -> transferEntries(navigator) }

    /**
     * Query argumentli deep link: `nav3bank://transfer?to={recipient}&from={fromCardId}`
     * → [O'tkazmalar tarixi, Forma(recipient, fromCardId)]. Ikkala argument ham ixtiyoriy.
     *
     * Test (`&` uchun butun buyruq qo'shtirnoq ichida):
     * adb shell 'am start -W -a android.intent.action.VIEW -d "nav3bank://transfer?to=8600111122223333&from=card-2" uz.latizx.navigation3'
     */
    @Provides
    @IntoSet
    fun provideTransferDeepLinks(): DeepLinkContributor = DeepLinkContributor {
        listOf(
            UriDeepLinkMatcher(
                uriPattern = Uri.parse("nav3bank://transfer?to={recipient}&from={fromCardId}"),
                serializer = TransferFormKey.serializer(),
                filters = listOf(DeepLinkMatcher.actionFilter(Intent.ACTION_VIEW)),
            ).withBackStack<TransferFormKey, NavKey> { match ->
                listOf(TransfersKey, match.key)
            },
        )
    }
}
