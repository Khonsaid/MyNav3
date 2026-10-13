package uz.latizx.mynav3.domain.model

data class UserProfile(
    val fullName: String,
    val phone: String,
    val language: AppLanguage,
)

enum class AppLanguage(val displayName: String) {
    UZ("O'zbekcha"),
    RU("Русский"),
    EN("English"),
}
