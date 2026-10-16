package uz.latizx.mynav3.presenter.settings.home

import uz.latizx.mynav3.domain.model.AppTheme

/** Sozlamalar bo'limlari. ViewModel NavKey'ni bilmaydi — faqat qaysi bo'lim bosilganini aytadi. */
enum class SettingsSection { SECURITY, NOTIFICATIONS, APPEARANCE, HELP, DELETE_ACCOUNT }

object SettingsHomeScreenContract {

    sealed interface Intent {
        data class SectionClicked(val section: SettingsSection) : Intent
    }

    data class UIState(
        val theme: AppTheme = AppTheme.SYSTEM,
    )

    sealed interface Effect {
        data class OpenSection(val section: SettingsSection) : Effect
    }
}