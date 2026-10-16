package uz.latizx.mynav3.presenter.settings.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
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
import uz.latizx.mynav3.core.navigation.LocalSharedViewModelStoreOwner
import uz.latizx.mynav3.core.navigation.NavTransitions
import uz.latizx.mynav3.core.navigation.Navigator
import uz.latizx.mynav3.core.navigation.SharedViewModelStoreNavEntryDecorator
import uz.latizx.mynav3.core.navigation.deeplink.DeepLinkContributor
import uz.latizx.mynav3.core.navigation.toContentKey
import uz.latizx.mynav3.domain.model.AppTheme
import uz.latizx.mynav3.presenter.appearance.AppearanceRoute
import uz.latizx.mynav3.presenter.appearance.AppearanceScreenContract
import uz.latizx.mynav3.presenter.appearance.AppearanceViewModel
import uz.latizx.mynav3.presenter.appearance.ThemeSheet
import uz.latizx.mynav3.presenter.auth.navigation.LoginKey
import uz.latizx.mynav3.presenter.deleteaccount.DeleteAccountDialog
import uz.latizx.mynav3.presenter.deleteaccount.DeleteAccountRoute
import uz.latizx.mynav3.presenter.deleteaccount.DeleteAccountScreenContract
import uz.latizx.mynav3.presenter.deleteaccount.DeleteAccountViewModel
import uz.latizx.mynav3.presenter.help.AboutScreen
import uz.latizx.mynav3.presenter.help.FaqDetailScreen
import uz.latizx.mynav3.presenter.help.HelpScreen
import uz.latizx.mynav3.presenter.notifications.NotificationCategoryRoute
import uz.latizx.mynav3.presenter.notifications.NotificationCategoryViewModel
import uz.latizx.mynav3.presenter.notifications.NotificationsRoute
import uz.latizx.mynav3.presenter.notifications.NotificationsScreenContract
import uz.latizx.mynav3.presenter.notifications.NotificationsViewModel
import uz.latizx.mynav3.presenter.notifications.QuietHoursDialog
import uz.latizx.mynav3.presenter.settings.home.SettingsHomeRoute
import uz.latizx.mynav3.presenter.settings.home.SettingsSection
import uz.latizx.mynav3.presenter.settings.security.SecurityRoute
import uz.latizx.mynav3.presenter.settings.security.changepin.ChangePinRoute
import uz.latizx.mynav3.presenter.settings.security.changepin.ChangePinStep
import uz.latizx.mynav3.presenter.settings.security.changepin.ChangePinViewModel
import uz.latizx.mynav3.presenter.settings.security.devices.DeviceDetailRoute
import uz.latizx.mynav3.presenter.settings.security.devices.DeviceDetailScreenContract
import uz.latizx.mynav3.presenter.settings.security.devices.DeviceDetailViewModel
import uz.latizx.mynav3.presenter.settings.security.devices.DevicesRoute
import uz.latizx.mynav3.presenter.settings.security.devices.TerminateDeviceDialog
import uz.latizx.mynav3.presenter.settings.security.pin.PinCheckRoute
import androidx.core.net.toUri

/**
 * SOZLAMALAR feature'i — Navigation 3 bo'yicha "mashq maydoni":
 *
 * ```
 * Sozlamalar (tab ildizi)
 *  ├─ Xavfsizlik ⇄ PIN darvozasi          shartli navigatsiya (replaceCurrent)
 *  │    ├─ PIN: joriy → yangi → tasdiq     umumiy ViewModel + popUpTo
 *  │    └─ Qurilmalar → Qurilma → dialog   3 qavat + argument + natija
 *  ├─ Bildirishnomalar → Kategoriya        enum argument, deep link
 *  │    └─ Jim soatlar (dialog)            natija (data class)
 *  ├─ Ko'rinish → Mavzu (bottom sheet)     natija (aniq kalit)
 *  ├─ Yordam → FAQ / Ilova haqida          ViewModel'siz ekranlar, slideUp
 *  └─ Akkauntni o'chirish → dialog         logout: hamma stack tozalanadi
 * ```
 */
fun EntryProviderScope<NavKey>.settingsEntries(navigator: Navigator) {

    // ───────────────────────────── Tab ildizi ─────────────────────────────

    entry<SettingsKey>(metadata = AppMetadata.topLevel()) {
        SettingsHomeRoute(
            viewModel = hiltViewModel(),
            // SettingsSection → NavKey: bog'lanish faqat shu faylda
            onOpenSection = { section ->
                navigator.navigate(
                    when (section) {
                        SettingsSection.SECURITY -> SecurityKey
                        SettingsSection.NOTIFICATIONS -> NotificationsKey
                        SettingsSection.APPEARANCE -> AppearanceKey
                        SettingsSection.HELP -> HelpKey
                        SettingsSection.DELETE_ACCOUNT -> DeleteAccountKey
                    },
                )
            },
        )
    }

    securityEntries(navigator)
    notificationEntries(navigator)
    appearanceEntries(navigator)
    helpEntries(navigator)
    deleteAccountEntries(navigator)
}

// ───────────────────────────── 1. Xavfsizlik ─────────────────────────────

private fun EntryProviderScope<NavKey>.securityEntries(navigator: Navigator) {

    // SHARTLI NAVIGATSIYA: PIN kiritilmagan bo'lsa Xavfsizlik o'zini darvoza bilan almashtiradi.
    //   [Sozlamalar, Xavfsizlik] → [Sozlamalar, PinCheck] → (to'g'ri PIN) → [Sozlamalar, Xavfsizlik]
    // replaceCurrent bo'lgani uchun Back hech qachon darvozaga yoki "qulf"li ekranga qaytarmaydi.
    entry<SecurityKey> {
        SecurityRoute(
            viewModel = hiltViewModel(),
            onRequirePin = { navigator.replaceCurrent(PinCheckKey) },
            onOpenChangePin = { navigator.navigate(ChangePinKey) },
            onOpenDevices = { navigator.navigate(DevicesKey) },
            onNavigateBack = navigator::goBack,
        )
    }

    entry<PinCheckKey>(metadata = NavTransitions.fade()) {
        PinCheckRoute(
            viewModel = hiltViewModel(),
            onUnlocked = { navigator.replaceCurrent(SecurityKey) },
            onNavigateBack = navigator::goBack,
        )
    }

    // UMUMIY VIEWMODEL (3 qadam). PARENT — 1-qadam: ViewModel shu entry'ning store'ida.
    // contentKey'ni o'zimiz beramiz — child'lar parent'ni aynan shu kalit bilan topadi.
    entry<ChangePinKey>(clazzContentKey = { key -> key.toContentKey() }) {
        ChangePinStepEntry(navigator, ChangePinStep.CURRENT, viewModel = hiltViewModel())
    }

    // CHILD'lar: metadata'da parent → decorator ularga parent'ning ViewModelStore'ini beradi.
    // ChangePinKey — data object, shuning uchun child key'larda uni saqlash shart emas.
    entry<ChangePinNewKey>(metadata = SharedViewModelStoreNavEntryDecorator.parent(ChangePinKey)) {
        ChangePinStepEntry(navigator, ChangePinStep.NEW, viewModel = sharedChangePinViewModel())
    }

    entry<ChangePinConfirmKey>(metadata = SharedViewModelStoreNavEntryDecorator.parent(ChangePinKey)) {
        ChangePinStepEntry(navigator, ChangePinStep.CONFIRM, viewModel = sharedChangePinViewModel())
    }

    entry<DevicesKey> {
        DevicesRoute(
            viewModel = hiltViewModel(),
            onOpenDevice = { deviceId -> navigator.navigate(DeviceDetailKey(deviceId)) },
            onNavigateBack = navigator::goBack,
        )
    }

    // 3-QAVAT + ARGUMENT + DIALOG NATIJASI
    entry<DeviceDetailKey> { key ->
        val viewModel = hiltViewModel<DeviceDetailViewModel, DeviceDetailViewModel.Factory>(
            creationCallback = { factory -> factory.create(key) },
        )
        // Dialog ochiq turganda ham bu entry chizilgan (dialog ostida) → natijani shu yerda olamiz
        ResultEffect<TerminateDeviceConfirmed> {
            viewModel.onIntent(DeviceDetailScreenContract.Intent.TerminateConfirmed)
        }
        DeviceDetailRoute(
            viewModel = viewModel,
            onOpenTerminateDialog = { name -> navigator.navigate(TerminateDeviceDialogKey(name)) },
            // Qurilma o'chirildi → ro'yxatga qaytamiz (ro'yxat Flow orqali o'zi yangilanadi)
            onTerminated = navigator::goBack,
            onNavigateBack = navigator::goBack,
        )
    }

    entry<TerminateDeviceDialogKey>(metadata = DialogSceneStrategy.dialog()) { key ->
        val resultBus = LocalResultEventBus.current
        TerminateDeviceDialog(
            deviceName = key.deviceName,
            onConfirm = {
                resultBus.sendResult(TerminateDeviceConfirmed)
                navigator.goBack()
            },
            onDismiss = navigator::goBack,
        )
    }
}

/** Uchala qadam bir xil: faqat [step] farq qiladi. Navigatsiya amallari "mutlaq". */
@Composable
private fun ChangePinStepEntry(navigator: Navigator, step: ChangePinStep, viewModel: ChangePinViewModel) {
    ChangePinRoute(
        viewModel = viewModel,
        step = step,
        onOpenNewStep = { navigator.navigate(ChangePinNewKey) },
        onOpenConfirmStep = { navigator.navigate(ChangePinConfirmKey) },
        // popUpTo: 3 ta qadam bir yo'la yopiladi → Xavfsizlik. Parent yopilgani uchun
        // umumiy ViewModel ham tozalanadi (keyingi safar bo'sh oqim boshlanadi).
        onFinished = { navigator.popUpTo(SecurityKey) },
        onNavigateBack = navigator::goBack,
    )
}

/** Parent (ChangePinKey) entry'si ishlatayotgan AYNAN O'SHA ViewModel. */
@Composable
private fun sharedChangePinViewModel(): ChangePinViewModel =
    hiltViewModel(viewModelStoreOwner = LocalSharedViewModelStoreOwner.current)

// ───────────────────────────── 2. Bildirishnomalar ─────────────────────────────

private fun EntryProviderScope<NavKey>.notificationEntries(navigator: Navigator) {

    entry<NotificationsKey> {
        val viewModel = hiltViewModel<NotificationsViewModel>()
        // Natija — data class: qaysi klass yuborilsa, shu klass nomi kalit bo'ladi
        ResultEffect<QuietHoursResult> { result ->
            viewModel.onIntent(NotificationsScreenContract.Intent.QuietHoursSelected(result.fromHour, result.toHour))
        }
        NotificationsRoute(
            viewModel = viewModel,
            onOpenCategory = { category -> navigator.navigate(NotificationCategoryKey(category)) },
            onOpenQuietHours = { from, to -> navigator.navigate(QuietHoursDialogKey(from, to)) },
            onNavigateBack = navigator::goBack,
        )
    }

    entry<NotificationCategoryKey> { key ->
        NotificationCategoryRoute(
            viewModel = hiltViewModel<NotificationCategoryViewModel, NotificationCategoryViewModel.Factory>(
                creationCallback = { factory -> factory.create(key) },
            ),
            onNavigateBack = navigator::goBack,
        )
    }

    entry<QuietHoursDialogKey>(metadata = DialogSceneStrategy.dialog()) { key ->
        val resultBus = LocalResultEventBus.current
        QuietHoursDialog(
            initialFrom = key.fromHour,
            initialTo = key.toHour,
            onConfirm = { from, to ->
                resultBus.sendResult(QuietHoursResult(from, to))
                navigator.goBack()
            },
            onDismiss = navigator::goBack,
        )
    }
}

// ───────────────────────────── 3. Ko'rinish ─────────────────────────────

private fun EntryProviderScope<NavKey>.appearanceEntries(navigator: Navigator) {

    entry<AppearanceKey> {
        val viewModel = hiltViewModel<AppearanceViewModel>()
        // AppTheme — enum. Umumiy tip bo'lgani uchun ANIQ kalit (LanguageSheet kabi)
        ResultEffect<AppTheme>(resultKey = ThemeSheetKey.RESULT_KEY) { theme ->
            viewModel.onIntent(AppearanceScreenContract.Intent.ThemeSelected(theme))
        }
        AppearanceRoute(
            viewModel = viewModel,
            onOpenThemeSheet = { current -> navigator.navigate(ThemeSheetKey(current)) },
            onNavigateBack = navigator::goBack,
        )
    }

    entry<ThemeSheetKey>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
        val resultBus = LocalResultEventBus.current
        ThemeSheet(
            current = key.current,
            onSelect = { theme ->
                resultBus.sendResult(ThemeSheetKey.RESULT_KEY, theme)
                navigator.goBack()
            },
        )
    }
}

// ───────────────────────────── 4. Yordam ─────────────────────────────

private fun EntryProviderScope<NavKey>.helpEntries(navigator: Navigator) {

    // ViewModel'siz ekranlar: lambda'lar to'g'ridan-to'g'ri Navigator'ga
    entry<HelpKey> {
        HelpScreen(
            onOpenFaq = { faqId -> navigator.navigate(FaqDetailKey(faqId)) },
            onOpenAbout = { navigator.navigate(AboutKey) },
            onNavigateBack = navigator::goBack,
        )
    }

    entry<FaqDetailKey> { key ->
        FaqDetailScreen(faqId = key.faqId, onNavigateBack = navigator::goBack)
    }

    // Ekran darajasidagi animatsiya: pastdan chiqadi (globaldagi o'ngdan surilish o'rniga)
    entry<AboutKey>(metadata = NavTransitions.slideUp()) {
        AboutScreen(onNavigateBack = navigator::goBack)
    }
}

// ───────────────────────────── 5. Akkauntni o'chirish ─────────────────────────────

private fun EntryProviderScope<NavKey>.deleteAccountEntries(navigator: Navigator) {

    entry<DeleteAccountKey> {
        val viewModel = hiltViewModel<DeleteAccountViewModel>()
        ResultEffect<DeleteAccountConfirmed> {
            viewModel.onIntent(DeleteAccountScreenContract.Intent.DeleteConfirmed)
        }
        DeleteAccountRoute(
            viewModel = viewModel,
            onOpenConfirmDialog = { navigator.navigate(DeleteAccountDialogKey) },
            // Hamma tab stack'lari tozalanadi (+ ularning ViewModel'lari) → Login
            onAccountDeleted = { navigator.logout(loginKey = LoginKey) },
            onNavigateBack = navigator::goBack,
        )
    }

    entry<DeleteAccountDialogKey>(metadata = DialogSceneStrategy.dialog()) {
        val resultBus = LocalResultEventBus.current
        DeleteAccountDialog(
            onConfirm = {
                resultBus.sendResult(DeleteAccountConfirmed)
                navigator.goBack()
            },
            onDismiss = navigator::goBack,
        )
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object SettingsNavigationModule {

    @Provides
    @IntoSet
    fun provideSettingsEntryInstaller(): EntryProviderInstaller = { navigator -> settingsEntries(navigator) }

    /**
     * Deep link'lar (sintetik back stack — Back bosilsa ilovadan chiqmasdan ota ekranga qaytadi):
     * ```
     * nav3bank://settings                          → [Sozlamalar]
     * nav3bank://settings/security                 → [Sozlamalar, Xavfsizlik]  (PIN darvozasi ishlaydi)
     * nav3bank://settings/notifications/TRANSFERS  → [Sozlamalar, Bildirishnomalar, Kategoriya]
     * nav3bank://settings/help/{faqId}             → [Sozlamalar, Yordam, FAQ]
     * ```
     */
    @Provides
    @IntoSet
    fun provideSettingsDeepLinks(): DeepLinkContributor = DeepLinkContributor {
        val view = listOf(DeepLinkMatcher.actionFilter(Intent.ACTION_VIEW))
        listOf(
            UriDeepLinkMatcher("nav3bank://settings".toUri(), SettingsKey.serializer(), view)
                .withBackStack<SettingsKey, NavKey> { match -> listOf(match.key) },

            UriDeepLinkMatcher(
                "nav3bank://settings/security".toUri(),
                SecurityKey.serializer(),
                view
            )
                .withBackStack<SecurityKey, NavKey> { match -> listOf(SettingsKey, match.key) },

            // {category} → NotificationCategoryKey.category (enum, nomi bilan: TRANSFERS, CARDS, PROMOTIONS).
            // Noto'g'ri nom kelsa — mos kelmaydi, ilova odatdagidek ochiladi.
            UriDeepLinkMatcher(
                "nav3bank://settings/notifications/{category}".toUri(),
                NotificationCategoryKey.serializer(),
                view,
            ).withBackStack<NotificationCategoryKey, NavKey> { match ->
                listOf(SettingsKey, NotificationsKey, match.key)
            },

            UriDeepLinkMatcher(
                "nav3bank://settings/help/{faqId}".toUri(),
                FaqDetailKey.serializer(),
                view
            ).withBackStack<FaqDetailKey, NavKey> { match -> listOf(SettingsKey, HelpKey, match.key) },
        )
    }
}