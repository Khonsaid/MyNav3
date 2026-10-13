package uz.latizx.mynav3.presenter.cards.list

import uz.latizx.mynav3.domain.model.Card

object CardsListScreenContract {

    sealed interface Intent {
        data class CardClicked(val cardId: String) : Intent
        data object RetryClicked : Intent
    }

    data class UIState(
        val isLoading: Boolean = true,
        val cards: List<Card> = emptyList(),
        val error: String? = null,
    ) {
        /** Bloklanmagan kartalardagi umumiy balans. */
        val totalBalance: Long
            get() = cards.filterNot { card -> card.isBlocked }.sumOf { card -> card.balance }
    }

    sealed interface Effect {
        data class OpenCardDetail(val cardId: String) : Effect
    }
}
