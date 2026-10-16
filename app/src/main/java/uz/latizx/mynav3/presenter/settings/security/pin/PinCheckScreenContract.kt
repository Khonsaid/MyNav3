package uz.latizx.mynav3.presenter.settings.security.pin

object PinCheckScreenContract {

    sealed interface Intent {
        data class PinChanged(val pin: String) : Intent
    }

    data class UIState(
        val pin: String = "",
        val isChecking: Boolean = false,
        val error: String? = null,
    )

    sealed interface Effect {
        /** PIN to'g'ri → navigatsiya qatlami darvozani Xavfsizlik ekrani bilan almashtiradi. */
        data object Unlocked : Effect
    }
}