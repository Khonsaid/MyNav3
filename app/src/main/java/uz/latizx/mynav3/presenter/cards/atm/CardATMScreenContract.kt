package uz.latizx.mynav3.presenter.cards.atm

import uz.latizx.mynav3.domain.model.Atm
import uz.latizx.mynav3.domain.model.Card
import kotlin.collections.filter

/**
 * "Shu karta ishlaydigan bankomatlar" ekrani kontrakti (MVI).
 *
 * - [Intent]  — foydalanuvchi nima qilmoqchi (UI → ViewModel)
 * - [UIState] — ekranda nima ko'rinadi (ViewModel → UI, doimiy holat)
 * - [Effect]  — bir martalik hodisa, asosan navigatsiya (ViewModel → Route → Navigator)
 */
object CardATMScreenContract {

    sealed interface Intent {
        /** ← tugmasi */
        data object BackClicked : Intent

        data object RetryClicked : Intent

        data class FilterSelected(val filter: AtmFilter) : Intent

        /** Bankomat tanlandi → tafsilot ekrani */
        data class AtmClicked(val atmId: String) : Intent

        /** "Kartalarga qaytish" → oradagi barcha ekranlar yopiladi (popUpTo) */
        data object BackToCardsClicked : Intent
    }

    data class UIState(
        val isLoading: Boolean = true,
        val card: Card? = null,
        val atms: List<Atm> = emptyList(),
        val filter: AtmFilter = AtmFilter.ALL,
        val error: String? = null,
    ) {
        /** Filtr qo'llangan ro'yxat (hisoblanadigan maydon — alohida saqlanmaydi). */
        val visibleAtms: List<Atm>
            get() = when (filter) {
                AtmFilter.ALL -> atms
                AtmFilter.OPEN_24_7 -> atms.filter { atm -> atm.isOpen24x7 }
                AtmFilter.CASH_IN -> atms.filter { atm -> atm.supportsCashIn }
            }
    }

    sealed interface Effect {
        data object NavigateBack : Effect
        data class OpenAtmDetail(val atmId: String) : Effect
        data object BackToCards : Effect
    }
}

enum class AtmFilter(val title: String) {
    ALL("Barchasi"),
    OPEN_24_7("24/7"),
    CASH_IN("Naqd pul qabul qiladi"),
}
