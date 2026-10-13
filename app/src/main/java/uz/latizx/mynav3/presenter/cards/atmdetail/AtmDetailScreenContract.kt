package uz.latizx.mynav3.presenter.cards.atmdetail

import uz.latizx.mynav3.domain.model.Atm

object AtmDetailScreenContract {

    sealed interface Intent {
        data object CloseClicked : Intent
        data object RetryClicked : Intent
    }

    data class UIState(
        val isLoading: Boolean = true,
        val atm: Atm? = null,
        val error: String? = null,
    )

    sealed interface Effect {
        data object NavigateBack : Effect
    }
}
