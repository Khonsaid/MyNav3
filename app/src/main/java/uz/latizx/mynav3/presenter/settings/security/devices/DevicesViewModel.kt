package uz.latizx.mynav3.presenter.settings.security.devices

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.repository.SecurityRepository
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    securityRepository: SecurityRepository,
) : BaseViewModel<DevicesScreenContract.Intent, DevicesScreenContract.UIState, DevicesScreenContract.Effect>(
    DevicesScreenContract.UIState()
) {

    init {
        // Flow: detal ekranida qurilma o'chirilsa, ro'yxat o'zi yangilanadi
        viewModelScope.launch {
            securityRepository.observeDevices().collect { devices ->
                setState { copy(isLoading = false, devices = devices) }
            }
        }
    }

    override fun handleIntent(intent: DevicesScreenContract.Intent) {
        when (intent) {
            is DevicesScreenContract.Intent.DeviceClicked -> sendEffect(
                DevicesScreenContract.Effect.OpenDevice(
                    intent.deviceId
                )
            )
        }
    }
}