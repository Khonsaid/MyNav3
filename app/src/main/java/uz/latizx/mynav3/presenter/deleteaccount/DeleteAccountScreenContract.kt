package uz.latizx.mynav3.presenter.deleteaccount

object DeleteAccountScreenContract {

    val REASONS = listOf(
        "Boshqa bankdan foydalanaman",
        "Ilova noqulay",
        "Xavfsizlik bo'yicha xavotir",
        "Boshqa sabab",
    )

    sealed interface Intent {
        data class ReasonSelected(val reason: String) : Intent
        data object ContinueClicked : Intent

        /** DeleteAccountDialog'dan qaytgan NATIJA. */
        data object DeleteConfirmed : Intent
    }

    data class UIState(
        val selectedReason: String? = null,
        val isDeleting: Boolean = false,
    )

    sealed interface Effect {
        data object OpenConfirmDialog : Effect

        /** Akkaunt o'chirildi → navigatsiya qatlami HAMMA stack'larni tozalab Login'ni ochadi. */
        data object AccountDeleted : Effect
    }
}