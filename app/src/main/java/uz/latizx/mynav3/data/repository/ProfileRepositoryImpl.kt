package uz.latizx.mynav3.data.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import uz.latizx.mynav3.data.source.FakeBankDataSource
import uz.latizx.mynav3.domain.model.AppLanguage
import uz.latizx.mynav3.domain.model.UserProfile
import uz.latizx.mynav3.domain.repository.ProfileRepository

class ProfileRepositoryImpl @Inject constructor(
    private val dataSource: FakeBankDataSource,
) : ProfileRepository {

    override fun observeProfile(): Flow<UserProfile> = dataSource.profile

    override suspend fun setLanguage(language: AppLanguage) {
        dataSource.networkDelay(300)
        dataSource.setLanguage(language)
    }
}
