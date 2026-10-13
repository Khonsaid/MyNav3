package uz.latizx.mynav3.domain.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import uz.latizx.mynav3.domain.model.AppLanguage
import uz.latizx.mynav3.domain.model.UserProfile
import uz.latizx.mynav3.domain.repository.ProfileRepository

class ObserveProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    operator fun invoke(): Flow<UserProfile> = profileRepository.observeProfile()
}

class ChangeLanguageUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(language: AppLanguage) = profileRepository.setLanguage(language)
}
