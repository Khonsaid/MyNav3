package uz.latizx.mynav3.domain.model

/** Bajarilgan o'tkazma. */
data class Transfer(
    val id: String,
    val fromCardId: String,
    val fromCardMasked: String,
    val recipient: String,
    val amount: Long,
    val createdAtMillis: Long,
)
