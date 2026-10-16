package uz.latizx.mynav3.presenter.settings.security.devices

import uz.latizx.mynav3.domain.model.Device

object DeviceDetailScreenContract {

    sealed interface Intent {
        data object TerminateClicked : Intent

        /** TerminateDeviceDialog'dan qaytgan NATIJA. */
        data object TerminateConfirmed : Intent
    }

    data class UIState(
        val isLoading: Boolean = true,
        val device: Device? = null,
        val isTerminating: Boolean = false,
    )

    sealed interface Effect {
        data class OpenTerminateDialog(val deviceName: String) : Effect

        /** Qurilma chiqarildi → detal ekrani yopiladi. */
        data object Terminated : Effect
    }
}