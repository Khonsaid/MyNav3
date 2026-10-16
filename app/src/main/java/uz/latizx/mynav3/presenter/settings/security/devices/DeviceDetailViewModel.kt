package uz.latizx.mynav3.presenter.settings.security.devices

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.repository.SecurityRepository
import uz.latizx.mynav3.presenter.settings.navigation.DeviceDetailKey

/** Argumentli ekran: key → ViewModel (Hilt assisted injection), CardDetailViewModel kabi. */
@HiltViewModel(assistedFactory = DeviceDetailViewModel.Factory::class)
class DeviceDetailViewModel @AssistedInject constructor(
    @Assisted private val navKey: DeviceDetailKey,
    private val securityRepository: SecurityRepository,
) : BaseViewModel<DeviceDetailScreenContract.Intent, DeviceDetailScreenContract.UIState,
        DeviceDetailScreenContract.Effect>(DeviceDetailScreenContract.UIState()) {

    @AssistedFactory
    interface Factory {
        fun create(navKey: DeviceDetailKey): DeviceDetailViewModel
    }

    init {
        // first(): o'chirilgandan keyin device = null bo'lib ekran "bo'sh" ko'rinmasligi uchun
        // kuzatmaymiz, bir marta o'qiymiz
        viewModelScope.launch {
            val device = securityRepository.observeDevice(navKey.deviceId).first()
            setState { copy(isLoading = false, device = device) }
        }
    }

    override fun handleIntent(intent: DeviceDetailScreenContract.Intent) {
        val device = currentState.device ?: return
        when (intent) {
            DeviceDetailScreenContract.Intent.TerminateClicked -> sendEffect(
                DeviceDetailScreenContract.Effect.OpenTerminateDialog(device.name)
            )

            DeviceDetailScreenContract.Intent.TerminateConfirmed -> {
                if (currentState.isTerminating) return
                viewModelScope.launch {
                    setState { copy(isTerminating = true) }
                    securityRepository.terminateDevice(device.id)
                    sendEffect(DeviceDetailScreenContract.Effect.Terminated)
                }
            }
        }
    }
}