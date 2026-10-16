package uz.latizx.mynav3.data.repository

import android.R.attr.theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import uz.latizx.mynav3.domain.model.AppTheme
import uz.latizx.mynav3.domain.model.CategorySettings
import uz.latizx.mynav3.domain.model.NotificationCategory
import uz.latizx.mynav3.domain.model.NotificationSettings
import uz.latizx.mynav3.domain.model.QuietHours
import uz.latizx.mynav3.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

/** Demo: xotirada saqlanadi. Haqiqiy loyihada — DataStore. */
@Singleton
class SettingsRepositoryImpl @Inject constructor() : SettingsRepository {
    private val theme = MutableStateFlow(AppTheme.SYSTEM)

    private val notifications = MutableStateFlow(
        NotificationSettings(
            categories = mapOf(
                NotificationCategory.TRANSFERS to CategorySettings(push = true, sms = true),
                NotificationCategory.CARDS to CategorySettings(push = true, sms = false),
                NotificationCategory.PROMOTIONS to CategorySettings(push = false, sms = false),
            ),
            quietHours = QuietHours(enabled = false, fromHour = 23, toHour = 7),
        ),
    )

    override fun observeTheme(): Flow<AppTheme> = theme

    override suspend fun setTheme(theme: AppTheme) {
        this.theme.value = theme
    }

    override fun observeNotifications(): Flow<NotificationSettings> = notifications

    override suspend fun setCategory(category: NotificationCategory, settings: CategorySettings) {
        delay(200.milliseconds)
        notifications.update { it.copy(categories = it.categories + (category to settings)) }
    }

    override suspend fun setQuietHours(quietHours: QuietHours) {
        delay(200.milliseconds)
        notifications.update { it.copy(quietHours = quietHours) }
    }
}