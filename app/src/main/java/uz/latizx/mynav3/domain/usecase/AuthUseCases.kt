package uz.latizx.mynav3.domain.usecase

import javax.inject.Inject
import uz.latizx.mynav3.domain.repository.AuthRepository

class CheckSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Boolean = authRepository.isLoggedIn()
}

class SendOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(phone: String): Result<Unit> = authRepository.sendOtp(phone)
}

class VerifyOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(phone: String, code: String): Result<Unit> =
        authRepository.verifyOtp(phone, code)
}

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() = authRepository.logout()
}
