package uz.latizx.mynav3.presenter.common

import uz.latizx.mynav3.domain.model.AppLanguage
import uz.latizx.mynav3.domain.model.Atm
import uz.latizx.mynav3.domain.model.Card
import uz.latizx.mynav3.domain.model.CardType
import uz.latizx.mynav3.domain.model.Transfer
import uz.latizx.mynav3.domain.model.UserProfile


/**
 * @Preview'lar uchun namunaviy ma'lumot. Route/Screen ajratilgani tufayli Screen'ni ViewModel'siz,
 * shunchaki `UIState` berib ko'rish mumkin.
 */
internal object PreviewData {

    val cards = listOf(
        Card(
            "card-1",
            "ALI VALIYEV",
            "8600123456781234",
            CardType.UZCARD,
            12_450_000,
            isBlocked = false
        ),
        Card("card-2", "ALI VALIYEV", "9860123456785678", CardType.HUMO, 3_200_500, isBlocked = false),
        Card("card-3", "ALI VALIYEV", "4278123456789012", CardType.VISA, 845_000, isBlocked = true),
    )

    val atms = listOf(
        Atm(
            id = "atm-1", name = "Bank filiali — Amir Temur", address = "Amir Temur ko'chasi, 15",
            workingHours = "24/7", isOpen24x7 = true, supportsCashIn = true,
            supportedCardTypes = setOf(CardType.UZCARD, CardType.HUMO), distanceMeters = 350,
        ),
        Atm(
            id = "atm-2",
            name = "Savdo markazi — Chilonzor",
            address = "Bunyodkor shoh ko'chasi, 3",
            workingHours = "09:00–22:00",
            isOpen24x7 = false,
            supportsCashIn = false,
            supportedCardTypes = setOf(CardType.UZCARD),
            distanceMeters = 1_200,
        ),
    )

    val transfers = listOf(
        Transfer(
            "tr-1",
            "card-1",
            "8600 •••• 1234",
            "9860444455556666",
            1_200_000,
            1_759_900_000_000
        ),
        Transfer(
            "tr-2",
            "card-2",
            "9860 •••• 5678",
            "8600111122223333",
            150_000,
            1_760_000_000_000
        ),
    )

    val profile = UserProfile(
        fullName = "Ali Valiyev",
        phone = "+998 90 123 45 67",
        language = AppLanguage.UZ
    )
}
