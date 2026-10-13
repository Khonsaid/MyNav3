package uz.latizx.mynav3.data.repository

import javax.inject.Inject
import uz.latizx.mynav3.data.source.FakeBankDataSource
import uz.latizx.mynav3.domain.model.Atm
import uz.latizx.mynav3.domain.repository.AtmRepository

class AtmRepositoryImpl @Inject constructor(
    private val dataSource: FakeBankDataSource,
) : AtmRepository {

    override suspend fun getAtms(): List<Atm> {
        dataSource.networkDelay()
        return dataSource.atms
    }

    override suspend fun getAtm(atmId: String): Atm? {
        dataSource.networkDelay(300)
        return dataSource.atms.firstOrNull { atm -> atm.id == atmId }
    }
}
