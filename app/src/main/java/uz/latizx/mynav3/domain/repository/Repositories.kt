package uz.latizx.mynav3.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.latizx.mynav3.domain.model.AppLanguage
import uz.latizx.mynav3.domain.model.Atm
import uz.latizx.mynav3.domain.model.Card
import uz.latizx.mynav3.domain.model.Transfer
import uz.latizx.mynav3.domain.model.UserProfile

// Domain qatlami faqat INTERFEYSLARNI biladi. Realizatsiya — `data` qatlamida
// (data/repository/...Impl klasslari), bog'lash — Hilt (data/di/RepositoryModule).

interface AuthRepository {
    suspend fun isLoggedIn(): Boolean
    suspend fun sendOtp(phone: String): Result<Unit>
    suspend fun verifyOtp(phone: String, code: String): Result<Unit>
    suspend fun logout()
}

interface CardRepository {
    fun observeCards(): Flow<List<Card>>
    fun observeCard(cardId: String): Flow<Card?>
    suspend fun setBlocked(cardId: String, blocked: Boolean)
}

interface AtmRepository {
    suspend fun getAtms(): List<Atm>
    suspend fun getAtm(atmId: String): Atm?
}

interface TransferRepository {
    fun observeTransfers(): Flow<List<Transfer>>
    suspend fun makeTransfer(fromCardId: String, recipient: String, amount: Long): Result<Transfer>
}

interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile>
    suspend fun setLanguage(language: AppLanguage)
}
