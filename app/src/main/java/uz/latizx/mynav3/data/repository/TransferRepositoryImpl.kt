package uz.latizx.mynav3.data.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import uz.latizx.mynav3.data.source.FakeBankDataSource
import uz.latizx.mynav3.domain.model.Transfer
import uz.latizx.mynav3.domain.repository.TransferRepository

class TransferRepositoryImpl @Inject constructor(
    private val dataSource: FakeBankDataSource,
) : TransferRepository {

    override fun observeTransfers(): Flow<List<Transfer>> = dataSource.transfers

    override suspend fun makeTransfer(
        fromCardId: String,
        recipient: String,
        amount: Long,
    ): Result<Transfer> {
        dataSource.networkDelay(1_000)
        val card = dataSource.cards.value.firstOrNull { it.id == fromCardId }
            ?: return Result.failure(IllegalStateException("Karta topilmadi"))
        if (card.isBlocked) return Result.failure(IllegalStateException("Karta bloklangan"))
        if (card.balance < amount) return Result.failure(IllegalStateException("Mablag' yetarli emas"))

        val transfer = Transfer(
            id = "tr-${System.currentTimeMillis()}",
            fromCardId = card.id,
            fromCardMasked = card.number.take(4) + " •••• " + card.number.takeLast(4),
            recipient = recipient,
            amount = amount,
            createdAtMillis = System.currentTimeMillis(),
        )
        dataSource.addTransfer(transfer)
        return Result.success(transfer)
    }
}
