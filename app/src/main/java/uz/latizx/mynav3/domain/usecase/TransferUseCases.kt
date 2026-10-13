package uz.latizx.mynav3.domain.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import uz.latizx.mynav3.domain.model.Transfer
import uz.latizx.mynav3.domain.repository.TransferRepository

class ObserveTransfersUseCase @Inject constructor(
    private val transferRepository: TransferRepository,
) {
    operator fun invoke(): Flow<List<Transfer>> = transferRepository.observeTransfers()
}

class MakeTransferUseCase @Inject constructor(
    private val transferRepository: TransferRepository,
) {
    suspend operator fun invoke(fromCardId: String, recipient: String, amount: Long): Result<Transfer> {
        if (amount <= 0) return Result.failure(IllegalArgumentException("Summa noto'g'ri"))
        if (recipient.length < 9) return Result.failure(IllegalArgumentException("Qabul qiluvchi noto'g'ri"))
        return transferRepository.makeTransfer(fromCardId, recipient, amount)
    }
}
