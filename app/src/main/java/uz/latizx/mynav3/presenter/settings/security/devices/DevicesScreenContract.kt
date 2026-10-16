package uz.latizx.mynav3.presenter.settings.security.devices

import uz.latizx.mynav3.domain.model.Device

object DevicesScreenContract {

    sealed interface Intent {
        data class DeviceClicked(val deviceId: String) : Intent
    }

    data class UIState(
        val isLoading: Boolean = true,
        val devices: List<Device> = emptyList(),
    )

    sealed interface Effect {
        data class OpenDevice(val deviceId: String) : Effect
    }
}