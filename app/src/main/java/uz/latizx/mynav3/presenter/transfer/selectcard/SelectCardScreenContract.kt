package uz.latizx.mynav3.presenter.transfer.selectcard

import uz.latizx.mynav3.domain.model.Card

object SelectCardScreenContract {

    sealed interface Intent {
        data class CardClicked(val cardId: String) : Intent
        data object BackClicked : Intent
    }

    data class UIState(
        val isLoading: Boolean = true,
        val cards: List<Card> = emptyList(),
        val selectedCardId: String? = null,
    )

    sealed interface Effect {
        /**
         * Karta tanlandi. Ekran keyin nima bo'lishini bilmaydi: navigatsiya qatlami
         * (TransferNavigation.kt) tanlovni o'tkazma oqimining umumiy ViewModel'iga yozadi
         * va bu ekranni yopadi. Boshqa oqimda xuddi shu ekran natijani ResultEventBus
         * orqali qaytarishi ham mumkin — ekran o'zgarmaydi.
         */
        data class CardChosen(val cardId: String) : Effect
        data object NavigateBack : Effect
    }
}
