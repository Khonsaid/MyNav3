package uz.latizx.mynav3.presenter.settings.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import uz.latizx.mynav3.domain.model.AppTheme
import uz.latizx.mynav3.domain.model.NotificationCategory

// ───────────────────────────── Tab ildizi ─────────────────────────────

@Serializable
data object SettingsKey : NavKey

// ───────────────────────────── 1. Xavfsizlik ─────────────────────────────

/** Xavfsizlik bo'limi. PIN kiritilmagan bo'lsa o'zini [PinCheckKey] bilan almashtiradi. */
@Serializable
data object SecurityKey : NavKey

/** PIN "darvozasi". To'g'ri PIN → o'zini [SecurityKey] bilan almashtiradi. */
@Serializable
data object PinCheckKey : NavKey

/** PIN almashtirish oqimi, 1-qadam (PARENT): umumiy ViewModel shu entry'da yashaydi. */
@Serializable
data object ChangePinKey : NavKey

/** 2-qadam (CHILD): yangi PIN. */
@Serializable
data object ChangePinNewKey : NavKey

/** 3-qadam (CHILD): yangi PIN'ni tasdiqlash. */
@Serializable
data object ChangePinConfirmKey : NavKey

@Serializable
data object DevicesKey : NavKey

@Serializable
data class DeviceDetailKey(val deviceId: String) : NavKey

/** DIALOG: qurilmadan chiqarishni tasdiqlash. */
@Serializable
data class TerminateDeviceDialogKey(val deviceName: String) : NavKey

/** NATIJA: TerminateDeviceDialog → DeviceDetail. */
data object TerminateDeviceConfirmed

// ───────────────────────────── 2. Bildirishnomalar ─────────────────────────────

@Serializable
data object NotificationsKey : NavKey

/** Bitta ekran — har xil argument. Enum deep link'da nomi bilan keladi (TRANSFERS, CARDS...). */
@Serializable
data class NotificationCategoryKey(val category: NotificationCategory) : NavKey

/** DIALOG: jim soatlar. Joriy qiymat key orqali beriladi, yangisi natija bo'lib qaytadi. */
@Serializable
data class QuietHoursDialogKey(val fromHour: Int, val toHour: Int) : NavKey

/** NATIJA: QuietHoursDialog → Notifications. */
data class QuietHoursResult(val fromHour: Int, val toHour: Int)

// ───────────────────────────── 3. Ko'rinish ─────────────────────────────

@Serializable
data object AppearanceKey : NavKey

/** BOTTOM SHEET: mavzu tanlash. Natija — [AppTheme], aniq kalit bilan. */
@Serializable
data class ThemeSheetKey(val current: AppTheme) : NavKey {
    companion object {
        const val RESULT_KEY = "uz.latizx.mynav3.ThemeSheet.result"
    }
}

// ───────────────────────────── 4. Yordam ─────────────────────────────

@Serializable
data object HelpKey : NavKey

@Serializable
data class FaqDetailKey(val faqId: String) : NavKey

@Serializable
data object AboutKey : NavKey

// ───────────────────────────── 5. Akkauntni o'chirish ─────────────────────────────

@Serializable
data object DeleteAccountKey : NavKey

@Serializable
data object DeleteAccountDialogKey : NavKey

/** NATIJA: DeleteAccountDialog → DeleteAccount. */
data object DeleteAccountConfirmed
