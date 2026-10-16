package uz.latizx.mynav3.domain.model

/** Ilova mavzusi (Ko'rinish bo'limi). */
enum class AppTheme(val displayName: String) {
    SYSTEM("Tizim bo'yicha"),
    LIGHT("Yorug'"),
    DARK("Qorong'i"),
}

/** Bildirishnoma turlari. Enum — NavKey argumenti va deep link'da nomi bilan keladi. */
enum class NotificationCategory(val displayName: String, val description: String) {
    TRANSFERS("O'tkazmalar", "Pul tushishi va yechilishi haqida"),
    CARDS("Kartalar", "Karta bloklanishi, limitlar"),
    PROMOTIONS("Aksiyalar", "Yangi takliflar va chegirmalar"),
}

data class CategorySettings(val push: Boolean, val sms: Boolean)

data class QuietHours(val enabled: Boolean, val fromHour: Int, val toHour: Int)

data class NotificationSettings(
    val categories: Map<NotificationCategory, CategorySettings>,
    val quietHours: QuietHours,
)

/** Hisobga kirilgan qurilma (Xavfsizlik → Faol qurilmalar). */
data class Device(
    val id: String,
    val name: String,
    val lastActive: String,
    val isCurrent: Boolean,
)