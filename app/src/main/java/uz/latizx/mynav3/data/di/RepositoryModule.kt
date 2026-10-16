package uz.latizx.mynav3.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.latizx.mynav3.data.repository.AtmRepositoryImpl
import uz.latizx.mynav3.data.repository.AuthRepositoryImpl
import uz.latizx.mynav3.data.repository.CardRepositoryImpl
import uz.latizx.mynav3.data.repository.ProfileRepositoryImpl
import uz.latizx.mynav3.data.repository.SecurityRepositoryImpl
import uz.latizx.mynav3.data.repository.SettingsRepositoryImpl
import uz.latizx.mynav3.data.repository.TransferRepositoryImpl
import uz.latizx.mynav3.domain.repository.AtmRepository
import uz.latizx.mynav3.domain.repository.AuthRepository
import uz.latizx.mynav3.domain.repository.CardRepository
import uz.latizx.mynav3.domain.repository.ProfileRepository
import uz.latizx.mynav3.domain.repository.SecurityRepository
import uz.latizx.mynav3.domain.repository.SettingsRepository
import uz.latizx.mynav3.domain.repository.TransferRepository

/** Domain interfeysi → data realizatsiyasi. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindCardRepository(impl: CardRepositoryImpl): CardRepository

    @Binds
    abstract fun bindAtmRepository(impl: AtmRepositoryImpl): AtmRepository

    @Binds
    abstract fun bindTransferRepository(impl: TransferRepositoryImpl): TransferRepository

    @Binds
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    abstract fun bindSecurityRepository(impl: SecurityRepositoryImpl): SecurityRepository
}
