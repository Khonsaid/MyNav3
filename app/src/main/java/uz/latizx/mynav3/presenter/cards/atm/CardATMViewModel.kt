package uz.latizx.mynav3.presenter.cards.atm

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.usecase.GetAtmsForCardUseCase
import uz.latizx.mynav3.presenter.cards.atm.CardATMScreenContract.Effect
import uz.latizx.mynav3.presenter.cards.atm.CardATMScreenContract.Intent
import uz.latizx.mynav3.presenter.cards.atm.CardATMScreenContract.UIState
import uz.latizx.mynav3.presenter.cards.navigation.CardATMKey

/**
 * [CardATMScreenContract] uchun ViewModel.
 *
 * Diqqat: bu yerda Navigator ham, NavKey yaratish ham YO'Q. "Bankomat tanlandi" →
 * `Effect.OpenAtmDetail(atmId)`. Uni `AtmDetailKey`ga aylantirish — CardsNavigation.kt ishi.
 */
@HiltViewModel(assistedFactory = CardATMViewModel.Factory::class)
class CardATMViewModel @AssistedInject constructor(
    @Assisted private val navKey: CardATMKey,
    private val getAtmsForCard: GetAtmsForCardUseCase,
) : BaseViewModel<Intent, UIState, Effect>(UIState()) {

    @AssistedFactory
    interface Factory {
        fun create(navKey: CardATMKey): CardATMViewModel
    }

    init {
        load()
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.BackClicked -> sendEffect(Effect.NavigateBack)
            Intent.RetryClicked -> load()
            is Intent.FilterSelected -> setState { copy(filter = intent.filter) }
            is Intent.AtmClicked -> sendEffect(Effect.OpenAtmDetail(intent.atmId))
            Intent.BackToCardsClicked -> sendEffect(Effect.BackToCards)
        }
    }

    private fun load() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            getAtmsForCard(navKey.cardId)
                .onSuccess { result ->
                    setState { copy(isLoading = false, card = result.card, atms = result.atms) }
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, error = error.message ?: "Xatolik") }
                }
        }
    }
}
