package uz.latizx.mynav3.presenter.notifications

import uz.latizx.mynav3.domain.model.NotificationCategory
import uz.latizx.mynav3.domain.model.NotificationSettings

object NotificationsScreenContract {

    sealed interface Intent {
        data class CategoryClicked(val category: NotificationCategory) : Intent
        data class QuietHoursToggled(val enabled: Boolean) : Intent
        data object QuietHoursClicked : Intent

        /** QuietHoursDialog'dan qaytgan NATIJA. */
        data class QuietHoursSelected(val fromHour: Int, val toHour: Int) : Intent
    }

    data class UIState(
        val isLoading: Boolean = true,
        val settings: NotificationSettings? = null,
    )

    sealed interface Effect {
        data class OpenCategory(val category: NotificationCategory) : Effect
        data class OpenQuietHours(val fromHour: Int, val toHour: Int) : Effect
    }
}