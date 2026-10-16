package uz.latizx.mynav3.presenter.appearance

import uz.latizx.mynav3.domain.model.AppTheme

object AppearanceScreenContract {

    sealed interface Intent {
        data object ThemeClicked : Intent

        /** ThemeSheet'dan qaytgan NATIJA. */
        data class ThemeSelected(val theme: AppTheme) : Intent
    }

    data class UIState(
        val theme: AppTheme = AppTheme.SYSTEM,
    )

    sealed interface Effect {
        data class OpenThemeSheet(val current: AppTheme) : Effect
    }
}