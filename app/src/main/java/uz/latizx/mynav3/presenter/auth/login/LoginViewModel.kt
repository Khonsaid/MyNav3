package uz.latizx.mynav3.presenter.auth.login

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.usecase.SendOtpUseCase
import uz.latizx.mynav3.presenter.auth.login.LoginScreenContract.Effect
import uz.latizx.mynav3.presenter.auth.login.LoginScreenContract.Intent
import uz.latizx.mynav3.presenter.auth.login.LoginScreenContract.PHONE_LENGTH
import uz.latizx.mynav3.presenter.auth.login.LoginScreenContract.UIState

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sendOtp: SendOtpUseCase,
) : BaseViewModel<Intent, UIState, Effect>(UIState()) {

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.PhoneChanged -> setState {
                copy(phone = intent.value.filter(Char::isDigit).take(PHONE_LENGTH), error = null)
            }

            Intent.ContinueClicked -> submit()
        }
    }

    private fun submit() {
        if (!currentState.isContinueEnabled) return
        val phone = currentState.phone
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            sendOtp(phone)
                .onSuccess {
                    setState { copy(isLoading = false) }
                    sendEffect(Effect.NavigateToOtp(phone))
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, error = error.message) }
                }
        }
    }
}
