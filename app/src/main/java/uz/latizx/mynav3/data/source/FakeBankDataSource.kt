package uz.latizx.mynav3.data.source

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uz.latizx.mynav3.domain.model.AppLanguage
import uz.latizx.mynav3.domain.model.Atm
import uz.latizx.mynav3.domain.model.Card
import uz.latizx.mynav3.domain.model.CardType
import uz.latizx.mynav3.domain.model.Transfer
import uz.latizx.mynav3.domain.model.UserProfile
import kotlin.time.Duration.Companion.milliseconds

/**
 * Soxta (in-memory) "server". Haqiqiy loyihada bu Retrofit/Ktor API va Room bo'ladi —
 * domain va presentation qatlamlari bundan bexabar, chunki ular faqat repository
 * interfeyslarini biladi.
 */
@Singleton
class FakeBankDataSource @Inject constructor() {

    private val _cards = MutableStateFlow(
        listOf(
            Card("card-1", "ALI VALIYEV", "8600123456781234", CardType.UZCARD, 12_450_000, isBlocked = false),
            Card("card-2", "ALI VALIYEV", "9860123456785678", CardType.HUMO, 3_200_500, isBlocked = false),
            Card("card-3", "ALI VALIYEV", "4278123456789012", CardType.VISA, 845_000, isBlocked = true),
        ),
    )
    val cards: StateFlow<List<Card>> = _cards.asStateFlow()

    private val _transfers = MutableStateFlow(
        listOf(
            Transfer("tr-2", "card-2", "9860 •••• 5678", "8600111122223333", 150_000, 1_760_000_000_000),
            Transfer("tr-1", "card-1", "8600 •••• 1234", "9860444455556666", 1_200_000, 1_759_900_000_000),
        ),
    )
    val transfers: StateFlow<List<Transfer>> = _transfers.asStateFlow()

    private val _profile = MutableStateFlow(
        UserProfile(fullName = "Ali Valiyev", phone = "+998 90 123 45 67", language = AppLanguage.UZ),
    )
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    val atms: List<Atm> = listOf(
        Atm(
            id = "atm-1", name = "Bank filiali — Amir Temur", address = "Amir Temur ko'chasi, 15",
            workingHours = "24/7", isOpen24x7 = true, supportsCashIn = true,
            supportedCardTypes = setOf(CardType.UZCARD, CardType.HUMO, CardType.VISA), distanceMeters = 350,
        ),
        Atm(
            id = "atm-2", name = "Savdo markazi — Chilonzor", address = "Bunyodkor shoh ko'chasi, 3",
            workingHours = "09:00–22:00", isOpen24x7 = false, supportsCashIn = true,
            supportedCardTypes = setOf(CardType.UZCARD, CardType.HUMO), distanceMeters = 1_200,
        ),
        Atm(
            id = "atm-3", name = "Metro — Mustaqillik maydoni", address = "Mustaqillik maydoni",
            workingHours = "05:00–24:00", isOpen24x7 = false, supportsCashIn = false,
            supportedCardTypes = setOf(CardType.UZCARD, CardType.HUMO), distanceMeters = 2_100,
        ),
        Atm(
            id = "atm-4", name = "Aeroport — Xalqaro terminal", address = "Kushbegi ko'chasi, 13",
            workingHours = "24/7", isOpen24x7 = true, supportsCashIn = false,
            supportedCardTypes = setOf(CardType.VISA, CardType.UZCARD), distanceMeters = 7_800,
        ),
        Atm(
            id = "atm-5", name = "Mehmonxona — Yunusobod", address = "Amir Temur shoh ko'chasi, 107",
            workingHours = "24/7", isOpen24x7 = true, supportsCashIn = true,
            supportedCardTypes = setOf(CardType.HUMO, CardType.VISA), distanceMeters = 4_500,
        ),
    )

    fun setCardBlocked(cardId: String, blocked: Boolean) {
        _cards.update { list ->
            list.map { card -> if (card.id == cardId) card.copy(isBlocked = blocked) else card }
        }
    }

    fun addTransfer(transfer: Transfer) {
        _transfers.update { list -> listOf(transfer) + list }
        _cards.update { list ->
            list.map { card ->
                if (card.id == transfer.fromCardId) card.copy(balance = card.balance - transfer.amount) else card
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        _profile.update { profile -> profile.copy(language = language) }
    }

    /** Tarmoq kechikishini taqlid qilish (loading holatlarini ko'rish uchun). */
    suspend fun networkDelay(millis: Long = 600) = delay(millis.milliseconds)
}
