package uz.latizx.mynav3.presenter.profile.navigation

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
import uz.latizx.mynav3.core.navigation.Navigator
import uz.latizx.mynav3.core.navigation.deeplink.DeepLinkContributor
import uz.latizx.mynav3.domain.model.AppLanguage
import uz.latizx.mynav3.presenter.auth.navigation.LoginKey
import uz.latizx.mynav3.presenter.profile.LanguageSheet
import uz.latizx.mynav3.presenter.profile.LogoutDialog
import uz.latizx.mynav3.presenter.profile.ProfileRoute
import uz.latizx.mynav3.presenter.profile.ProfileScreenContract
import uz.latizx.mynav3.presenter.profile.ProfileViewModel
import uz.latizx.mynav3.presenter.settings.navigation.NotificationsKey
import uz.latizx.mynav3.presenter.settings.navigation.SettingsKey

/**
 * PROFIL feature'i:
 *  - BOTTOM SHEET'dan natija (til) — ANIQ kalit bilan (`LanguageSheetKey.RESULT_KEY`);
 *  - DIALOG'dan natija (chiqish) — klass nomi kaliti bilan;
 *  - LOGOUT: barcha tab stack'lari tozalanadi → Login (boshqa feature'ning key'i).
 */
fun EntryProviderScope<NavKey>.profileEntries(navigator: Navigator) {

    entry<ProfileKey>(metadata = AppMetadata.topLevel()) {
        val viewModel = hiltViewModel<ProfileViewModel>()

        // Aniq kalitli natija: yuboruvchi ham xuddi shu kalitni ishlatadi (pastda)
        ResultEffect<AppLanguage>(resultKey = LanguageSheetKey.RESULT_KEY) { language ->
            viewModel.onIntent(ProfileScreenContract.Intent.LanguageSelected(language))
        }
        ResultEffect<LogoutConfirmedResult> {
            viewModel.onIntent(ProfileScreenContract.Intent.LogoutConfirmed)
        }

        ProfileRoute(
            viewModel = viewModel,
            onOpenLanguageSheet = { current -> navigator.navigate(LanguageSheetKey(current)) },
            onOpenLogoutDialog = { navigator.navigate(LogoutDialogKey) },
            // Logout: barcha ekranlar (va ularning ViewModel'lari) tozalanadi
            onLoggedOut = { navigator.logout(loginKey = LoginKey) },
            // Sozlamalar tabi tanlanadi va uning stack'i [Sozlamalar, Bildirishnomalar] bo'ladi.
            // Back → Sozlamalar ro'yxati → (yana Back) → uy tab.
            onOpenNotificationSettings = { navigator.navigateInTab(SettingsKey, NotificationsKey) },
        )
    }

    entry<LanguageSheetKey>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
        val resultBus = LocalResultEventBus.current
        LanguageSheet(
            current = key.current,
            onSelect = { language ->
                resultBus.sendResult(LanguageSheetKey.RESULT_KEY, language)
                navigator.goBack()
            },
        )
    }

    entry<LogoutDialogKey>(metadata = DialogSceneStrategy.dialog()) {
        val resultBus = LocalResultEventBus.current
        LogoutDialog(
            onConfirm = {
                resultBus.sendResult(LogoutConfirmedResult)
                navigator.goBack()
            },
            onDismiss = navigator::goBack,
        )
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object ProfileNavigationModule {

    @Provides
    @IntoSet
    fun provideProfileEntryInstaller(): EntryProviderInstaller =
        { navigator -> profileEntries(navigator) }

    /**
     * Argumentsiz (aniq yo'l) deep link: `nav3bank://profile` → [Profil].
     * adb shell am start -W -a android.intent.action.VIEW -d "nav3bank://profile" uz.latizx.navigation3
     */
    @Provides
    @IntoSet
    fun provideProfileDeepLinks(): DeepLinkContributor = DeepLinkContributor {
        listOf(
            UriDeepLinkMatcher(
                uriPattern = Uri.parse("nav3bank://profile"),
                serializer = ProfileKey.serializer(),
                filters = listOf(DeepLinkMatcher.actionFilter(Intent.ACTION_VIEW)),
            ).withBackStack<ProfileKey, NavKey> { match -> listOf(match.key) },
        )
    }
}
