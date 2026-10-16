package uz.latizx.mynav3.presenter.notifications

import uz.latizx.mynav3.domain.model.CategorySettings
import uz.latizx.mynav3.domain.model.NotificationCategory


object NotificationCategoryScreenContract {

    sealed interface Intent {
        data class PushToggled(val enabled: Boolean) : Intent
        data class SmsToggled(val enabled: Boolean) : Intent
    }

    data class UIState(
        val category: NotificationCategory,
        val settings: CategorySettings? = null,
    )

    /** Bu ekranda bir martalik hodisa yo'q (Back — oddiy lambda). */
    sealed interface Effect
}