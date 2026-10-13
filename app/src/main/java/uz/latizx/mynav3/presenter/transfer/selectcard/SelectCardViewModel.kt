package uz.latizx.mynav3.presenter.transfer.selectcard

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.usecase.ObserveCardsUseCase
import uz.latizx.mynav3.presenter.transfer.navigation.SelectCardKey
import uz.latizx.mynav3.presenter.transfer.selectcard.SelectCardScreenContract.Effect
import uz.latizx.mynav3.presenter.transfer.selectcard.SelectCardScreenContract.Intent
import uz.latizx.mynav3.presenter.transfer.selectcard.SelectCardScreenContract.UIState

@HiltViewModel(assistedFactory = SelectCardViewModel.Factory::class)
class SelectCardViewModel @AssistedInject constructor(
    @Assisted navKey: SelectCardKey,
    private val observeCards: ObserveCardsUseCase,
) : BaseViewModel<Intent, UIState, Effect>(UIState(selectedCardId = navKey.selectedCardId)) {

    @AssistedFactory
    interface Factory {
        fun create(navKey: SelectCardKey): SelectCardViewModel
    }

    init {
        viewModelScope.launch {
            observeCards().collect { cards -> setState { copy(isLoading = false, cards = cards) } }
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.CardClicked -> {
                val card = currentState.cards.firstOrNull { it.id == intent.cardId } ?: return
                if (card.isBlocked) return // bloklangan kartadan o'tkazma yo'q
                setState { copy(selectedCardId = card.id) }
                sendEffect(Effect.CardChosen(card.id))
            }

            Intent.BackClicked -> sendEffect(Effect.NavigateBack)
        }
    }
}
