package uz.latizx.mynav3.domain.model

/** Bank kartasi (domain modeli — Android/Compose/serialization'ga bog'liq emas). */
data class Card(
    val id: String,
    val holderName: String,
    val number: String,
    val type: CardType,
    /** Balans tiyinsiz, so'mda. */
    val balance: Long,
    val isBlocked: Boolean,
)

enum class CardType(val title: String) {
    UZCARD("Uzcard"),
    HUMO("Humo"),
    VISA("Visa"),
}
