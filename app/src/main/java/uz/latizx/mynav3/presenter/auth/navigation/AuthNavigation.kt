package uz.latizx.mynav3.presenter.auth.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import uz.latizx.mynav3.core.navigation.EntryProviderInstaller
import uz.latizx.mynav3.core.navigation.NavTransitions
import uz.latizx.mynav3.core.navigation.Navigator
import uz.latizx.mynav3.presenter.auth.login.LoginRoute
import uz.latizx.navigation3.presentation.auth.otp.OtpRoute
import uz.latizx.mynav3.presenter.auth.otp.OtpViewModel
import uz.latizx.mynav3.presenter.auth.splash.SplashRoute

/**
 * AUTH oqimi ekranlari (conditional navigation).
 *
 * ```
 * Splash ──(sessiya bor)──────────────────────────────▶ completeAuthFlow() ─▶ Kartalar
 *    └──(sessiya yo'q)── replaceCurrent(Login) ── Login ── navigate(Otp) ── Otp ──┘
 * ```
 * `completeAuthFlow()` auth stack'ni tozalaydi → asosiy ekrandan Back bosilsa Login'ga
 * QAYTILMAYDI, ilova yopiladi. Splash ham `replaceCurrent` bilan almashtiriladi.
 */
fun EntryProviderScope<NavKey>.authEntries(navigator: Navigator) {

    entry<SplashKey>(metadata = NavTransitions.fade()) {
        SplashRoute(
            viewModel = hiltViewModel(),
            onNavigateToMain = navigator::completeAuthFlow,
            onNavigateToLogin = { navigator.replaceCurrent(LoginKey) },
        )
    }

    entry<LoginKey>(metadata = NavTransitions.fade()) {
        LoginRoute(
            viewModel = hiltViewModel(),
            onNavigateToOtp = { phone -> navigator.navigate(OtpKey(phone)) },
        )
    }

    entry<OtpKey> { key ->
        OtpRoute(
            // Argumentli ekran: key → ViewModel (Hilt assisted injection).
            // Har bir entry o'z ViewModelStore'iga ega (SharedViewModelStoreNavEntryDecorator),
            // shuning uchun OtpKey("90...") va OtpKey("91...") alohida ViewModel oladi.
            viewModel = hiltViewModel<OtpViewModel, OtpViewModel.Factory>(
                creationCallback = { factory -> factory.create(key) },
            ),
            onVerified = navigator::completeAuthFlow,
            onNavigateBack = navigator::goBack,
        )
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object AuthNavigationModule {

    @Provides
    @IntoSet
    fun provideAuthEntryInstaller(): EntryProviderInstaller = { navigator -> authEntries(navigator) }
}
