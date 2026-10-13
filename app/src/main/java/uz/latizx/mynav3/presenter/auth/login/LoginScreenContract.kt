package uz.latizx.mynav3.presenter.auth.login

object LoginScreenContract {

    const val PHONE_LENGTH = 9

    sealed interface Intent {
        data class PhoneChanged(val value: String) : Intent
        data object ContinueClicked : Intent
    }

    data class UIState(
        /** Faqat raqamlar, "+998"siz: 901234567 */
        val phone: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
    ) {
        val isContinueEnabled: Boolean
            get() = phone.length == PHONE_LENGTH && !isLoading
    }

    sealed interface Effect {
        data class NavigateToOtp(val phone: String) : Effect
    }
}
