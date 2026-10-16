package uz.latizx.mynav3.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.latizx.mynav3.domain.model.AppTheme
import uz.latizx.mynav3.domain.model.CategorySettings
import uz.latizx.mynav3.domain.model.Device
import uz.latizx.mynav3.domain.model.NotificationCategory
import uz.latizx.mynav3.domain.model.NotificationSettings
import uz.latizx.mynav3.domain.model.QuietHours

interface SettingsRepository {
    fun observeTheme(): Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)

    fun observeNotifications(): Flow<NotificationSettings>
    suspend fun setCategory(category: NotificationCategory, settings: CategorySettings)
    suspend fun setQuietHours(quietHours: QuietHours)
}

interface SecurityRepository {
    /** Shu sessiyada PIN kiritilganmi (Xavfsizlik bo'limi "ochiq"mi). */
    fun isUnlocked(): Boolean

    /** To'g'ri bo'lsa bo'limni ochadi (isUnlocked = true). */
    suspend fun unlock(pin: String): Boolean

    /** Faqat tekshiradi, holatni o'zgartirmaydi (PIN almashtirishning 1-qadami). */
    suspend fun checkPin(pin: String): Boolean
    suspend fun changePin(oldPin: String, newPin: String): Result<Unit>

    fun observeBiometric(): Flow<Boolean>
    suspend fun setBiometric(enabled: Boolean)

    fun observeDevices(): Flow<List<Device>>
    fun observeDevice(deviceId: String): Flow<Device?>
    suspend fun terminateDevice(deviceId: String)

    suspend fun deleteAccount(reason: String)
}