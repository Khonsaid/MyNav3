package uz.latizx.mynav3.presenter.transfer.form

import uz.latizx.mynav3.domain.model.Card


/**
 * O'tkazma OQIMINING kontrakti. Uning ViewModel'i (TransferFormViewModel) UMUMIY: forma,
 * kartani tanlash ekrani va tasdiqlash dialogi bitta nusxani ishlatadi
 * (qarang: TransferNavigation.kt, SharedViewModelStoreNavEntryDecorator).
 * Intent'larni qaysi ekran yuborishi izohlarda ko'rsatilgan.
 */
object TransferFormScreenContract {

    const val MIN_RECIPIENT_LENGTH = 9

    sealed interface Intent {
        data class RecipientChanged(val value: String) : Intent
        data class AmountChanged(val value: String) : Intent
        data object SelectCardClicked : Intent

        /** KARTANI TANLASH ekranidan (child): tanlangan karta umumiy holatga yoziladi. */
        data class CardSelected(val cardId: String) : Intent

        data object ContinueClicked : Intent

        /** TASDIQLASH dialogidan (child): o'tkazmani bajarish. */
        data object TransferConfirmed : Intent

        /** ← tugmasi yoki tizim Back (BackHandler ushlab oladi). */
        data object BackClicked : Intent
        data object DiscardConfirmed : Intent
        data object DiscardDismissed : Intent
    }

    data class UIState(
        val isLoading: Boolean = true,
        val cards: List<Card> = emptyList(),
        val selectedCard: Card? = null,
        val recipient: String = "",
        /** Deep link / "Takrorlash"dan kelgan boshlang'ich qiymat (o'zgarmagan bo'lsa — "toza"). */
        val prefilledRecipient: String = "",
        val amount: String = "",
        val isSubmitting: Boolean = false,
        val error: String? = null,
        val showDiscardDialog: Boolean = false,
    ) {
        val amountValue: Long
            get() = amount.toLongOrNull() ?: 0L

        /** Foydalanuvchi biror narsa kiritganmi? (Back'da "o'zgarishlar yo'qolsinmi?" so'rash uchun) */
        val isDirty: Boolean
            get() = amount.isNotEmpty() || recipient != prefilledRecipient

        val canContinue: Boolean
            get() = selectedCard != null &&
                recipient.length >= MIN_RECIPIENT_LENGTH &&
                amountValue > 0 &&
                !isSubmitting
    }

    sealed interface Effect {
        data class OpenSelectCard(val selectedCardId: String?) : Effect

        /** Tasdiqlash dialogi ochilsin. Ma'lumot uzatilmaydi — dialog umumiy holatdan o'qiydi. */
        data object OpenConfirm : Effect
        data class OpenSuccess(val transferId: String, val amount: Long) : Effect
        data object NavigateBack : Effect
    }
}
