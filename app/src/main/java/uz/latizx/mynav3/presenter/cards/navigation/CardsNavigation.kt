package uz.latizx.mynav3.presenter.cards.navigation

import android.content.Intent
import android.net.Uri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.actionFilter
import androidx.navigation3.runtime.deeplink.withBackStack
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEffect
import androidx.navigation3.scene.DialogSceneStrategy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import uz.latizx.mynav3.core.navigation.AppMetadata
import uz.latizx.mynav3.core.navigation.BottomSheetSceneStrategy
import uz.latizx.mynav3.core.navigation.EntryProviderInstaller
import uz.latizx.mynav3.core.navigation.NavTransitions
import uz.latizx.mynav3.core.navigation.Navigator
import uz.latizx.mynav3.core.navigation.deeplink.DeepLinkContributor
import uz.latizx.mynav3.presenter.cards.atm.CardATMRoute
import uz.latizx.mynav3.presenter.cards.atm.CardATMViewModel
import uz.latizx.mynav3.presenter.cards.atmdetail.AtmDetailRoute
import uz.latizx.mynav3.presenter.cards.atmdetail.AtmDetailViewModel
import uz.latizx.mynav3.presenter.cards.detail.CardDetailRoute
import uz.latizx.mynav3.presenter.cards.detail.CardDetailScreenContract
import uz.latizx.mynav3.presenter.cards.detail.CardDetailViewModel
import uz.latizx.mynav3.presenter.cards.dialog.BlockCardDialog
import uz.latizx.mynav3.presenter.cards.dialog.CardActionsSheet
import uz.latizx.mynav3.presenter.cards.list.CardsListRoute
import uz.latizx.mynav3.presenter.transfer.navigation.TransferFormKey
import uz.latizx.mynav3.presenter.transfer.navigation.TransfersKey

/**
 * KARTALAR feature'ining navigatsiya grafi. Bu fayl — feature'dagi YAGONA joy, u yerda
 * Effect → NavKey bog'lanadi. Ekranlar (Route/Screen) va ViewModel'lar navigatsiyani bilmaydi.
 *
 * Ko'rsatilgan holatlar:
 *  1. Tab ildizi (pastki panel ko'rinadi, tablar orasida fade)
 *  2. Argument uzatish (CardDetailKey(cardId)) + assisted ViewModel
 *  3. DIALOG manzili va undan NATIJA qaytarish (ResultEventBus)
 *  4. BOTTOM SHEET manzili va undan boshqa ekranga o'tish (replaceCurrent)
 *  5. CROSS-TAB: karta tafsilotidan O'tkazmalar tabidagi formaga
 *  6. popUpTo: bankomatlar ekranidan to'g'ridan-to'g'ri kartalar ro'yxatiga
 *  7. Custom animatsiya: ATM tafsiloti pastdan chiqadi
 *  8. Deep link'lar (sintetik back stack bilan) — pastdagi Hilt modulida
 *  9. SHARED ELEMENT: karta ro'yxatdan detal ekraniga "uchib" o'tadi — bu faylda hech narsa
 *     qilinmaydi, faqat ikkala ekranda `Modifier.navSharedElement(bankCardSharedKey(id))`
 */
fun EntryProviderScope<NavKey>.cardsEntries(navigator: Navigator) {

    // 1) Kartalar ro'yxati: tab ildizi (pastki panel + fade)
    entry<CardsKey>(metadata = AppMetadata.topLevel()) {
        CardsListRoute(
            viewModel = hiltViewModel(),
            onOpenCard = { cardId -> navigator.navigate(CardDetailKey(cardId)) },
        )
    }

    // 2) Karta tafsiloti. Argument key orqali ViewModel'ga beriladi.
    entry<CardDetailKey> { key ->
        val viewModel = hiltViewModel<CardDetailViewModel, CardDetailViewModel.Factory>(
            creationCallback = { factory -> factory.create(key) },
        )

        // 3) NATIJANI QABUL QILISH: BlockCardDialog yopilganda shu yerga keladi.
        //    Natija → Intent (MVI): qaror ViewModel'da qabul qilinadi.
        ResultEffect<BlockCardResult> { result ->
            if (result.cardId == key.cardId) {
                viewModel.onIntent(CardDetailScreenContract.Intent.BlockConfirmed(result.block))
            }
        }

        CardDetailRoute(
            viewModel = viewModel,
            onNavigateBack = navigator::goBack,
            onOpenAtms = { cardId -> navigator.navigate(CardATMKey(cardId)) },
            // 5) CROSS-TAB navigatsiya
            onOpenTransfer = { cardId ->
                navigator.navigateInTab(TransfersKey, TransferFormKey(fromCardId = cardId))
            },
            onOpenBlockDialog = { cardId, block ->
                navigator.navigate(BlockCardDialogKey(cardId = cardId, block = block))
            },
            onOpenActions = { cardId, isBlocked ->
                navigator.navigate(CardActionsSheetKey(cardId = cardId, isBlocked = isBlocked))
            },
        )
    }

    // Foydalanuvchi ko'rsatgan kontrakt: CardATMScreenContract (Intent / UIState / Effect).
    entry<CardATMKey> { key ->
        CardATMRoute(
            viewModel = hiltViewModel<CardATMViewModel, CardATMViewModel.Factory>(
                creationCallback = { factory -> factory.create(key) },
            ),
            onNavigateBack = navigator::goBack,
            onOpenAtmDetail = { atmId -> navigator.navigate(AtmDetailKey(atmId)) },
            // 6) popUpTo: oradagi barcha ekranlarni yopib, ro'yxatga qaytish
            onBackToCards = { navigator.popUpTo(CardsKey) },
        )
    }

    // 7) Entry darajasidagi animatsiya: pastdan yuqoriga (global o'ngdan-chapga o'rniga)
    entry<AtmDetailKey>(metadata = NavTransitions.slideUp()) { key ->
        AtmDetailRoute(
            viewModel = hiltViewModel<AtmDetailViewModel, AtmDetailViewModel.Factory>(
                creationCallback = { factory -> factory.create(key) },
            ),
            onNavigateBack = navigator::goBack,
        )
    }

    // 3) DIALOG manzili: DialogSceneStrategy uni oldingi ekran USTIDA ko'rsatadi.
    //    Natija yuborish: sendResult(...) → goBack(). Qabul qiluvchi — ResultEffect (yuqorida).
    entry<BlockCardDialogKey>(metadata = DialogSceneStrategy.dialog()) { key ->
        val resultBus = LocalResultEventBus.current
        BlockCardDialog(
            block = key.block,
            onConfirm = {
                resultBus.sendResult(BlockCardResult(cardId = key.cardId, block = key.block))
                navigator.goBack()
            },
            onDismiss = navigator::goBack,
        )
    }

    // 4) BOTTOM SHEET manzili. Sheet'dagi tanlov boshqa ekranni ochadi:
    //    replaceCurrent — sheet o'rniga yangi ekran (Back bosilsa sheet qayta ochilmaydi).
    entry<CardActionsSheetKey>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
        CardActionsSheet(
            isBlocked = key.isBlocked,
            onAtmsClick = { navigator.replaceCurrent(CardATMKey(key.cardId)) },
            onTransferClick = {
                navigator.goBack() // avval sheet'ni yopamiz (Kartalar stack'idan)
                navigator.navigateInTab(TransfersKey, TransferFormKey(fromCardId = key.cardId))
            },
            onBlockToggleClick = {
                navigator.replaceCurrent(
                    BlockCardDialogKey(
                        cardId = key.cardId,
                        block = !key.isBlocked
                    )
                )
            },
            onClose = navigator::goBack,
        )
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object CardsNavigationModule {

    @Provides
    @IntoSet
    fun provideCardsEntryInstaller(): EntryProviderInstaller = { navigator -> cardsEntries(navigator) }

    /**
     * 8) DEEP LINK'lar. `withBackStack` — sintetik back stack: deep link orqali to'g'ridan-to'g'ri
     * karta tafsilotiga kelgan foydalanuvchi Back bosganda ilovadan chiqib ketmaydi, balki
     * kartalar ro'yxatiga qaytadi.
     *
     * Test:
     * adb shell am start -W -a android.intent.action.VIEW -d "nav3bank://cards/card-2" uz.latizx.navigation3
     * adb shell am start -W -a android.intent.action.VIEW -d "nav3bank://cards/card-1/atms" uz.latizx.navigation3
     */
    @Provides
    @IntoSet
    fun provideCardsDeepLinks(): DeepLinkContributor = DeepLinkContributor {
        val viewOnly = listOf(DeepLinkMatcher.actionFilter(Intent.ACTION_VIEW))
        listOf(
            // nav3bank://cards/{cardId} → [Kartalar, Karta tafsiloti]
            UriDeepLinkMatcher(
                uriPattern = Uri.parse("nav3bank://cards/{cardId}"),
                serializer = CardDetailKey.serializer(),
                filters = viewOnly,
            ).withBackStack<CardDetailKey, NavKey> { match ->
                listOf(CardsKey, match.key)
            },
            // nav3bank://cards/{cardId}/atms → [Kartalar, Karta tafsiloti, Bankomatlar]
            UriDeepLinkMatcher(
                uriPattern = Uri.parse("nav3bank://cards/{cardId}/atms"),
                serializer = CardATMKey.serializer(),
                filters = viewOnly,
            ).withBackStack<CardATMKey, NavKey> { match ->
                listOf(CardsKey, CardDetailKey(match.key.cardId), match.key)
            },
        )
    }
}
