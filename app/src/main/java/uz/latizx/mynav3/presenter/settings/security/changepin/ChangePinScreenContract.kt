package uz.latizx.mynav3.presenter.settings.security.changepin

/** Oqim qadamlari. Har bir qadam — alohida NavKey, lekin ViewModel bitta (umumiy). */
enum class ChangePinStep(val title: String) {
    CURRENT("Joriy PIN-kodni kiriting"),
    NEW("Yangi PIN-kodni kiriting"),
    CONFIRM("Yangi PIN-kodni takrorlang"),
}

object ChangePinScreenContract {

    sealed interface Intent {
        data class PinChanged(val step: ChangePinStep, val pin: String) : Intent
    }

    data class UIState(
        val currentPin: String = "",
        val newPin: String = "",
        val confirmPin: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
    ) {
        fun pinFor(step: ChangePinStep): String = when (step) {
            ChangePinStep.CURRENT -> currentPin
            ChangePinStep.NEW -> newPin
            ChangePinStep.CONFIRM -> confirmPin
        }
    }

    sealed interface Effect {
        data object OpenNewStep : Effect
        data object OpenConfirmStep : Effect

        /** PIN almashtirildi → butun oqim bir yo'la yopiladi (popUpTo). */
        data object Finished : Effect
    }
}