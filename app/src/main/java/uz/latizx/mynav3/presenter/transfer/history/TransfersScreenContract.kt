package uz.latizx.mynav3.presenter.transfer.history

import uz.latizx.mynav3.domain.model.Transfer


object TransfersScreenContract {

    sealed interface Intent {
        data object NewTransferClicked : Intent

        /** "Takrorlash" — formani bir nechta argument bilan ochish. */
        data class RepeatClicked(val transfer: Transfer) : Intent
    }

    data class UIState(
        val isLoading: Boolean = true,
        val transfers: List<Transfer> = emptyList(),
    )

    sealed interface Effect {
        data object OpenNewTransfer : Effect
        data class OpenRepeatTransfer(val fromCardId: String, val recipient: String) : Effect
    }
}
