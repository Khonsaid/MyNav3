package uz.latizx.mynav3.presenter.auth.otp

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.usecase.SendOtpUseCase
import uz.latizx.mynav3.domain.usecase.VerifyOtpUseCase
import uz.latizx.mynav3.presenter.auth.navigation.OtpKey
import uz.latizx.mynav3.presenter.auth.otp.OtpScreenContract.CODE_LENGTH
import uz.latizx.mynav3.presenter.auth.otp.OtpScreenContract.Effect
import uz.latizx.mynav3.presenter.auth.otp.OtpScreenContract.Intent
import uz.latizx.mynav3.presenter.auth.otp.OtpScreenContract.RESEND_TIMEOUT_SECONDS
import uz.latizx.mynav3.presenter.auth.otp.OtpScreenContract.UIState
import kotlin.time.Duration.Companion.milliseconds

/**
 * NAVIGATSIYA ARGUMENTI → VIEWMODEL (Hilt assisted injection).
 *
 * Nav2'da argument `SavedStateHandle["phone"]` orqali string sifatida olinardi.
 * Nav3'da esa NavKey'ning o'zi ([OtpKey]) tip-xavfsiz holda konstruktorga beriladi:
 * ```
 * hiltViewModel<OtpViewModel, OtpViewModel.Factory>(
 *     creationCallback = { factory -> factory.create(key) }
 * )
 * ```
 */
@HiltViewModel(assistedFactory = OtpViewModel.Factory::class)
class OtpViewModel @AssistedInject constructor(
    @Assisted private val navKey: OtpKey,
    private val verifyOtp: VerifyOtpUseCase,
    private val sendOtp: SendOtpUseCase,
) : BaseViewModel<Intent, UIState, Effect>(UIState(phone = navKey.phone)) {

    @AssistedFactory
    interface Factory {
        fun create(navKey: OtpKey): OtpViewModel
    }

    private var timerJob: Job? = null

    init {
        startResendTimer()
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.CodeChanged -> {
                val code = intent.value.filter(Char::isDigit).take(CODE_LENGTH)
                setState { copy(code = code, error = null) }
                // 4-raqam kiritilishi bilan avtomatik tekshiramiz
                if (code.length == CODE_LENGTH) verify()
            }

            Intent.VerifyClicked -> verify()
            Intent.ResendClicked -> resend()
            Intent.BackClicked -> sendEffect(Effect.NavigateBack)
        }
    }

    private fun verify() {
        val state = currentState
        if (!state.canVerify) return
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            verifyOtp(navKey.phone, state.code)
                .onSuccess {
                    setState { copy(isLoading = false) }
                    sendEffect(Effect.NavigateToMain)
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, code = "", error = error.message) }
                }
        }
    }

    private fun resend() {
        if (!currentState.canResend) return
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            sendOtp(navKey.phone)
                .onSuccess {
                    setState { copy(isLoading = false) }
                    startResendTimer()
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            setState { copy(resendSeconds = RESEND_TIMEOUT_SECONDS) }
            while (currentState.resendSeconds > 0) {
                delay(1_000.milliseconds)
                setState { copy(resendSeconds = resendSeconds - 1) }
            }
        }
    }
}
