package uz.latizx.mynav3.presenter.cards.list

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.usecase.ObserveCardsUseCase
import uz.latizx.mynav3.presenter.cards.list.CardsListScreenContract.Effect
import uz.latizx.mynav3.presenter.cards.list.CardsListScreenContract.Intent
import uz.latizx.mynav3.presenter.cards.list.CardsListScreenContract.UIState

/**
 * Tab ildizining ViewModel'i. Kartalar → O'tkazmalar → Kartalar tablarini almashtirganda bu
 * ViewModel QAYTA YARATILMAYDI: entry o'z stack'idan olib tashlanmagani uchun uning
 * ViewModelStore'i saqlanib turadi (multiple back stacks).
 */
@HiltViewModel
class CardsListViewModel @Inject constructor(
    private val observeCards: ObserveCardsUseCase,
) : BaseViewModel<Intent, UIState, Effect>(UIState()) {

    private var observeJob: Job? = null

    init {
        observe()
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.CardClicked -> sendEffect(Effect.OpenCardDetail(intent.cardId))
            Intent.RetryClicked -> observe()
        }
    }

    private fun observe() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            observeCards()
                .catch { error -> setState { copy(isLoading = false, error = error.message ?: "Xatolik") } }
                .collect { cards -> setState { copy(isLoading = false, cards = cards, error = null) } }
        }
    }
}
