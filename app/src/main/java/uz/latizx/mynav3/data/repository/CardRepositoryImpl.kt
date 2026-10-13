package uz.latizx.mynav3.data.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import uz.latizx.mynav3.data.source.FakeBankDataSource
import uz.latizx.mynav3.domain.model.Card
import uz.latizx.mynav3.domain.repository.CardRepository

class CardRepositoryImpl @Inject constructor(
    private val dataSource: FakeBankDataSource,
) : CardRepository {

    override fun observeCards(): Flow<List<Card>> =
        dataSource.cards.onStart { dataSource.networkDelay() }

    override fun observeCard(cardId: String): Flow<Card?> =
        dataSource.cards
            .map { cards -> cards.firstOrNull { card -> card.id == cardId } }
            .distinctUntilChanged()

    override suspend fun setBlocked(cardId: String, blocked: Boolean) {
        dataSource.networkDelay(400)
        dataSource.setCardBlocked(cardId, blocked)
    }
}
