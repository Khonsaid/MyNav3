package uz.latizx.mynav3.presenter.auth.splash

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.usecase.CheckSessionUseCase
import uz.latizx.mynav3.presenter.auth.splash.SplashScreenContract.Effect
import uz.latizx.mynav3.presenter.auth.splash.SplashScreenContract.Intent
import uz.latizx.mynav3.presenter.auth.splash.SplashScreenContract.UIState

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkSession: CheckSessionUseCase,
) : BaseViewModel<Intent, UIState, Effect>(UIState()) {

    init {
        startSessionCheck()
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.RetryClicked -> startSessionCheck()
        }
    }

    private fun startSessionCheck() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            runCatching { checkSession() }
                .onSuccess { isLoggedIn ->
                    sendEffect(if (isLoggedIn) Effect.NavigateToMain else Effect.NavigateToLogin)
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, error = error.message ?: "Xatolik yuz berdi") }
                }
        }
    }
}
