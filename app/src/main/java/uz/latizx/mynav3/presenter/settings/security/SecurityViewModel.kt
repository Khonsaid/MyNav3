package uz.latizx.mynav3.presenter.settings.security

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.repository.SecurityRepository
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val securityRepository: SecurityRepository,
) : BaseViewModel<SecurityScreenContract.Intent, SecurityScreenContract.UIState, SecurityScreenContract.Effect>(
    SecurityScreenContract.UIState()
) {

    init {
        // DARVOZA shu yerda: qaysi yo'l bilan kelinmasin (ro'yxatdan, deep link'dan,
        // boshqa tabdan) — Xavfsizlik ekrani o'zi tekshiradi.
        if (securityRepository.isUnlocked()) {
            setState { copy(isUnlocked = true) }
            viewModelScope.launch {
                securityRepository.observeBiometric().collect { enabled ->
                    setState { copy(biometricEnabled = enabled) }
                }
            }
        } else {
            sendEffect(SecurityScreenContract.Effect.RequirePin)
        }
    }

    override fun handleIntent(intent: SecurityScreenContract.Intent) {
        when (intent) {
            is SecurityScreenContract.Intent.BiometricToggled -> viewModelScope.launch { securityRepository.setBiometric(intent.enabled) }
            SecurityScreenContract.Intent.ChangePinClicked -> sendEffect(SecurityScreenContract.Effect.OpenChangePin)
            SecurityScreenContract.Intent.DevicesClicked -> sendEffect(SecurityScreenContract.Effect.OpenDevices)
        }
    }
}