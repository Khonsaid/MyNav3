package uz.latizx.mynav3.domain.model

/** Bankomat. */
data class Atm(
    val id: String,
    val name: String,
    val address: String,
    val workingHours: String,
    val isOpen24x7: Boolean,
    val supportsCashIn: Boolean,
    val supportedCardTypes: Set<CardType>,
    val distanceMeters: Int,
)

/** Karta va shu karta ishlaydigan bankomatlar (CardATM ekrani uchun). */
data class CardWithAtms(
    val card: Card,
    val atms: List<Atm>,
)
