package uz.latizx.mynav3.presenter.cards.detail

import uz.latizx.mynav3.domain.model.Card

object CardDetailScreenContract {

    sealed interface Intent {
        data object BackClicked : Intent
        data object RetryClicked : Intent
        data object AtmsClicked : Intent
        data object TransferClicked : Intent
        data object BlockToggleClicked : Intent
        data object MoreClicked : Intent

        /** BlockCardDialog'dan qaytgan NATIJA (ResultEffect → Intent). */
        data class BlockConfirmed(val block: Boolean) : Intent
    }

    data class UIState(
        val isLoading: Boolean = true,
        val card: Card? = null,
        val isUpdating: Boolean = false,
        val error: String? = null,
    )

    sealed interface Effect {
        data object NavigateBack : Effect
        data class OpenAtms(val cardId: String) : Effect
        data class OpenTransfer(val cardId: String) : Effect
        data class OpenBlockDialog(val cardId: String, val block: Boolean) : Effect
        data class OpenActions(val cardId: String, val isBlocked: Boolean) : Effect
    }
}
