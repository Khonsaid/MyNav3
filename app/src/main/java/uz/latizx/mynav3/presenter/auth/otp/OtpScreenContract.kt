package uz.latizx.mynav3.presenter.auth.otp

object OtpScreenContract {

    const val CODE_LENGTH = 4
    const val RESEND_TIMEOUT_SECONDS = 30

    sealed interface Intent {
        data class CodeChanged(val value: String) : Intent
        data object VerifyClicked : Intent
        data object ResendClicked : Intent
        data object BackClicked : Intent
    }

    data class UIState(
        val phone: String,
        val code: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
        val resendSeconds: Int = RESEND_TIMEOUT_SECONDS,
    ) {
        val canVerify: Boolean
            get() = code.length == CODE_LENGTH && !isLoading

        val canResend: Boolean
            get() = resendSeconds == 0 && !isLoading
    }

    sealed interface Effect {
        /** Kod to'g'ri → auth oqimi tugaydi (Login/OTP back stack'dan o'chiriladi). */
        data object NavigateToMain : Effect
        data object NavigateBack : Effect
    }
}
