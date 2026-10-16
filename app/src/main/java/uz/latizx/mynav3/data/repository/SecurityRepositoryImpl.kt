package uz.latizx.mynav3.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import uz.latizx.mynav3.domain.model.Device
import uz.latizx.mynav3.domain.repository.SecurityRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

/**
 * Demo: PIN = [DEMO_PIN]. `unlocked` faqat xotirada — ilova qayta ishga tushsa,
 * Xavfsizlik bo'limi yana PIN so'raydi.
 */
@Singleton
class SecurityRepositoryImpl @Inject constructor() : SecurityRepository {
    private var pin = DEMO_PIN
    private var unlocked = false
    private val biometric = MutableStateFlow(false)

    private val devices = MutableStateFlow(
        listOf(
            Device("dev-1", "Pixel 9 (shu qurilma)", "Hozir faol", isCurrent = true),
            Device("dev-2", "Samsung Galaxy A54", "Kecha, 18:40", isCurrent = false),
            Device("dev-3", "iPad Air", "3 kun oldin", isCurrent = false),
        ),
    )

    override fun isUnlocked(): Boolean = unlocked

    override suspend fun unlock(pin: String): Boolean {
        delay(400.milliseconds)
        unlocked = pin == this.pin
        return unlocked
    }

    override suspend fun checkPin(pin: String): Boolean {
        delay(300.milliseconds)
        return pin == this.pin
    }

    override suspend fun changePin(oldPin: String, newPin: String): Result<Unit> {
        delay(500.milliseconds)
        if (oldPin != pin) return Result.failure(IllegalStateException("Joriy PIN noto'g'ri"))
        pin = newPin
        return Result.success(Unit)
    }

    override fun observeBiometric(): Flow<Boolean> = biometric

    override suspend fun setBiometric(enabled: Boolean) {
        biometric.value = enabled
    }

    override fun observeDevices(): Flow<List<Device>> = devices

    override fun observeDevice(deviceId: String): Flow<Device?> =
        devices.map { list -> list.firstOrNull { it.id == deviceId } }

    override suspend fun terminateDevice(deviceId: String) {
        delay(500.milliseconds)
        devices.update { list -> list.filterNot { it.id == deviceId } }
    }

    override suspend fun deleteAccount(reason: String) {
        delay(800.milliseconds)
        unlocked = false
    }

    companion object {
        const val DEMO_PIN = "0000"
    }
}