package uz.latizx.mynav3.presenter.settings.security.pin

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.repository.SecurityRepository
import javax.inject.Inject

@HiltViewModel
class PinCheckViewModel @Inject constructor(
    private val securityRepository: SecurityRepository,
) : BaseViewModel<PinCheckScreenContract.Intent, PinCheckScreenContract.UIState, PinCheckScreenContract.Effect>(
    PinCheckScreenContract.UIState()
) {

    override fun handleIntent(intent: PinCheckScreenContract.Intent) {
        when (intent) {
            is PinCheckScreenContract.Intent.PinChanged -> {
                setState { copy(pin = intent.pin, error = null) }
                if (intent.pin.length == PIN_LENGTH) check(intent.pin)
            }
        }
    }

    private fun check(pin: String) {
        viewModelScope.launch {
            setState { copy(isChecking = true) }
            if (securityRepository.unlock(pin)) {
                sendEffect(PinCheckScreenContract.Effect.Unlocked)
            } else {
                setState { copy(isChecking = false, pin = "", error = "PIN noto'g'ri") }
            }
        }
    }
}