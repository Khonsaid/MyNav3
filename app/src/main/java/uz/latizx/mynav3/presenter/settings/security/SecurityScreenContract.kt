package uz.latizx.mynav3.presenter.settings.security

object SecurityScreenContract {

    sealed interface Intent {
        data class BiometricToggled(val enabled: Boolean) : Intent
        data object ChangePinClicked : Intent
        data object DevicesClicked : Intent
    }

    data class UIState(
        /** false — PIN hali tekshirilmagan: ekran hech narsa ko'rsatmaydi, darvozaga o'tamiz. */
        val isUnlocked: Boolean = false,
        val biometricEnabled: Boolean = false,
    )

    sealed interface Effect {
        /** PIN kiritilmagan → navigatsiya qatlami shu ekranni PIN darvozasi bilan almashtiradi. */
        data object RequirePin : Effect
        data object OpenChangePin : Effect
        data object OpenDevices : Effect
    }
}