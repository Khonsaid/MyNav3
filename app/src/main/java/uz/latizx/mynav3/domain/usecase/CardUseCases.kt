package uz.latizx.mynav3.domain.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import uz.latizx.mynav3.domain.model.Atm
import uz.latizx.mynav3.domain.model.Card
import uz.latizx.mynav3.domain.model.CardWithAtms
import uz.latizx.mynav3.domain.repository.AtmRepository
import uz.latizx.mynav3.domain.repository.CardRepository

class ObserveCardsUseCase @Inject constructor(
    private val cardRepository: CardRepository,
) {
    operator fun invoke(): Flow<List<Card>> = cardRepository.observeCards()
}

class ObserveCardUseCase @Inject constructor(
    private val cardRepository: CardRepository,
) {
    operator fun invoke(cardId: String): Flow<Card?> = cardRepository.observeCard(cardId)
}

class SetCardBlockedUseCase @Inject constructor(
    private val cardRepository: CardRepository,
) {
    suspend operator fun invoke(cardId: String, blocked: Boolean) =
        cardRepository.setBlocked(cardId, blocked)
}

/** Karta va shu karta TURI qabul qilinadigan bankomatlar (masofa bo'yicha saralangan). */
class GetAtmsForCardUseCase @Inject constructor(
    private val cardRepository: CardRepository,
    private val atmRepository: AtmRepository,
) {
    suspend operator fun invoke(cardId: String): Result<CardWithAtms> = runCatching {
        val card = cardRepository.observeCard(cardId).first()
            ?: error("Karta topilmadi: $cardId")
        val atms = atmRepository.getAtms()
            .filter { atm -> card.type in atm.supportedCardTypes }
            .sortedBy { atm -> atm.distanceMeters }
        CardWithAtms(card = card, atms = atms)
    }
}

class GetAtmUseCase @Inject constructor(
    private val atmRepository: AtmRepository,
) {
    suspend operator fun invoke(atmId: String): Atm? = atmRepository.getAtm(atmId)
}
