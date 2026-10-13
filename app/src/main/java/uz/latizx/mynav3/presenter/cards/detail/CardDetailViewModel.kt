package uz.latizx.mynav3.presenter.cards.detail

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.usecase.ObserveCardUseCase
import uz.latizx.mynav3.domain.usecase.SetCardBlockedUseCase
import uz.latizx.mynav3.presenter.cards.detail.CardDetailScreenContract.Effect
import uz.latizx.mynav3.presenter.cards.detail.CardDetailScreenContract.Intent
import uz.latizx.mynav3.presenter.cards.detail.CardDetailScreenContract.UIState
import uz.latizx.mynav3.presenter.cards.navigation.CardDetailKey

@HiltViewModel(assistedFactory = CardDetailViewModel.Factory::class)
class CardDetailViewModel @AssistedInject constructor(
    @Assisted private val navKey: CardDetailKey,
    private val observeCard: ObserveCardUseCase,
    private val setCardBlocked: SetCardBlockedUseCase,
) : BaseViewModel<Intent, UIState, Effect>(UIState()) {

    @AssistedFactory
    interface Factory {
        fun create(navKey: CardDetailKey): CardDetailViewModel
    }

    private var observeJob: Job? = null

    init {
        observe()
    }

    override fun handleIntent(intent: Intent) {
        val card = currentState.card
        when (intent) {
            Intent.BackClicked -> sendEffect(Effect.NavigateBack)
            Intent.RetryClicked -> observe()
            Intent.AtmsClicked -> sendEffect(Effect.OpenAtms(navKey.cardId))
            Intent.TransferClicked -> sendEffect(Effect.OpenTransfer(navKey.cardId))
            Intent.BlockToggleClicked -> if (card != null) {
                sendEffect(Effect.OpenBlockDialog(cardId = card.id, block = !card.isBlocked))
            }

            Intent.MoreClicked -> if (card != null) {
                sendEffect(Effect.OpenActions(cardId = card.id, isBlocked = card.isBlocked))
            }

            is Intent.BlockConfirmed -> updateBlocked(intent.block)
        }
    }

    private fun observe() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            observeCard(navKey.cardId)
                .catch { error -> setState { copy(isLoading = false, error = error.message ?: "Xatolik") } }
                .collect { card ->
                    setState {
                        if (card == null) {
                            copy(isLoading = false, card = null, error = "Karta topilmadi")
                        } else {
                            copy(isLoading = false, card = card, error = null)
                        }
                    }
                }
        }
    }

    private fun updateBlocked(block: Boolean) {
        viewModelScope.launch {
            setState { copy(isUpdating = true) }
            runCatching { setCardBlocked(navKey.cardId, block) }
            // Karta holati observeCard() oqimi orqali avtomatik yangilanadi
            setState { copy(isUpdating = false) }
        }
    }
}
