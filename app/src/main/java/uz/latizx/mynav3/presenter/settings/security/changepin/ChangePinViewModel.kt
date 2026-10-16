package uz.latizx.mynav3.presenter.settings.security.changepin

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.repository.SecurityRepository
import uz.latizx.mynav3.presenter.settings.security.pin.PIN_LENGTH
import javax.inject.Inject

/**
 * UMUMIY ViewModel: 3 ta ekran (CURRENT → NEW → CONFIRM) bitta nusxani ishlatadi.
 * Yashash joyi — 1-qadam (ChangePinKey) entry'si; 2- va 3-qadam unga `parent(...)` orqali ulanadi.
 * Shuning uchun 3-qadamda 1-qadamda kiritilgan joriy PIN hali ham bor.
 */
@HiltViewModel
class ChangePinViewModel @Inject constructor(
    private val securityRepository: SecurityRepository,
) : BaseViewModel<ChangePinScreenContract.Intent, ChangePinScreenContract.UIState, ChangePinScreenContract.Effect>(ChangePinScreenContract.UIState()) {

    override fun handleIntent(intent: ChangePinScreenContract.Intent) {
        when (intent) {
            is ChangePinScreenContract.Intent.PinChanged -> onPinChanged(intent.step, intent.pin)
        }
    }

    private fun onPinChanged(step: ChangePinStep, pin: String) {
        setState {
            when (step) {
                ChangePinStep.CURRENT -> copy(currentPin = pin, error = null)
                ChangePinStep.NEW -> copy(newPin = pin, error = null)
                ChangePinStep.CONFIRM -> copy(confirmPin = pin, error = null)
            }
        }
        if (pin.length < PIN_LENGTH) return

        when (step) {
            ChangePinStep.CURRENT -> viewModelScope.launch {
                setState { copy(isLoading = true) }
                if (securityRepository.checkPin(pin)) {
                    setState { copy(isLoading = false) }
                    sendEffect(ChangePinScreenContract.Effect.OpenNewStep)
                } else {
                    setState { copy(isLoading = false, currentPin = "", error = "Joriy PIN noto'g'ri") }
                }
            }

            ChangePinStep.NEW -> if (pin == currentState.currentPin) {
                setState { copy(newPin = "", error = "Yangi PIN eskisidan farq qilishi kerak") }
            } else {
                setState { copy(confirmPin = "") }
                sendEffect(ChangePinScreenContract.Effect.OpenConfirmStep)
            }

            ChangePinStep.CONFIRM -> if (pin != currentState.newPin) {
                setState { copy(confirmPin = "", error = "PIN'lar mos kelmadi") }
            } else {
                viewModelScope.launch {
                    setState { copy(isLoading = true) }
                    securityRepository.changePin(currentState.currentPin, pin)
                        .onSuccess { sendEffect(ChangePinScreenContract.Effect.Finished) }
                        .onFailure { e ->
                            setState { copy(isLoading = false, confirmPin = "", error = e.message) }
                        }
                }
            }
        }
    }
}