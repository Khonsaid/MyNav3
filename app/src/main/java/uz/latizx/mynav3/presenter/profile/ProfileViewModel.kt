package uz.latizx.mynav3.presenter.profile

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.usecase.ChangeLanguageUseCase
import uz.latizx.mynav3.domain.usecase.LogoutUseCase
import uz.latizx.mynav3.domain.usecase.ObserveProfileUseCase
import uz.latizx.mynav3.presenter.profile.ProfileScreenContract.Effect
import uz.latizx.mynav3.presenter.profile.ProfileScreenContract.Intent
import uz.latizx.mynav3.presenter.profile.ProfileScreenContract.UIState

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val observeProfile: ObserveProfileUseCase,
    private val changeLanguage: ChangeLanguageUseCase,
    private val logout: LogoutUseCase,
) : BaseViewModel<Intent, UIState, Effect>(UIState()) {

    init {
        viewModelScope.launch {
            observeProfile().collect { profile ->
                setState { copy(isLoading = false, profile = profile) }
            }
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.LanguageClicked -> {
                val language = currentState.profile?.language ?: return
                sendEffect(Effect.OpenLanguageSheet(language))
            }

            is Intent.LanguageSelected -> viewModelScope.launch { changeLanguage(intent.language) }

            Intent.LogoutClicked -> sendEffect(Effect.OpenLogoutDialog)
            Intent.NotificationSettingsClicked -> sendEffect(Effect.OpenNotificationSettings)
            Intent.LogoutConfirmed -> {
                if (currentState.isLoggingOut) return
                viewModelScope.launch {
                    setState { copy(isLoggingOut = true) }
                    logout()
                    sendEffect(Effect.LoggedOut)
                }
            }
        }
    }
}
