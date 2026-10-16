package uz.latizx.mynav3.presenter.profile

import uz.latizx.mynav3.domain.model.AppLanguage
import uz.latizx.mynav3.domain.model.UserProfile

object ProfileScreenContract {

    sealed interface Intent {
        data object LanguageClicked : Intent

        /** LanguageSheet'dan qaytgan NATIJA. */
        data class LanguageSelected(val language: AppLanguage) : Intent

        data object LogoutClicked : Intent
        data object NotificationSettingsClicked : Intent
        /** LogoutDialog'dan qaytgan NATIJA. */
        data object LogoutConfirmed : Intent
    }

    data class UIState(
        val isLoading: Boolean = true,
        val profile: UserProfile? = null,
        val isLoggingOut: Boolean = false,
    )

    sealed interface Effect {
        data class OpenLanguageSheet(val current: AppLanguage) : Effect
        data object OpenLogoutDialog : Effect
        data object OpenNotificationSettings : Effect
        /** Sessiya tugadi → navigatsiya qatlami barcha stack'larni tozalab Login'ni ochadi. */
        data object LoggedOut : Effect
    }
}
