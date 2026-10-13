package uz.latizx.mynav3.presenter.auth.splash

object SplashScreenContract {

    sealed interface Intent {
        data object RetryClicked : Intent
    }

    data class UIState(
        val isLoading: Boolean = true,
        val error: String? = null,
    )

    sealed interface Effect {
        /** Sessiya bor → asosiy ekran (auth stack tozalanadi). */
        data object NavigateToMain : Effect

        /** Sessiya yo'q → Login (Splash almashtiriladi, Back bilan unga qaytilmaydi). */
        data object NavigateToLogin : Effect
    }
}
