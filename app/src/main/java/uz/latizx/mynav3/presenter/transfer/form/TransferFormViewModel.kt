package uz.latizx.mynav3.presenter.transfer.form

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.usecase.MakeTransferUseCase
import uz.latizx.mynav3.domain.usecase.ObserveCardsUseCase
import uz.latizx.mynav3.presenter.transfer.form.TransferFormScreenContract.Effect
import uz.latizx.mynav3.presenter.transfer.form.TransferFormScreenContract.Intent
import uz.latizx.mynav3.presenter.transfer.form.TransferFormScreenContract.UIState
import uz.latizx.mynav3.presenter.transfer.navigation.TransferFormKey

/**
 * O'tkazma OQIMINING UMUMIY ViewModel'i. Egasi — forma entry'si (PARENT); kartani tanlash ekrani
 * va tasdiqlash dialogi (CHILD'lar) AYNAN SHU nusxani `LocalSharedViewModelStoreOwner` orqali
 * oladi. ViewModel buni bilmaydi ham — u oddiy MVI ViewModel, kim Intent yuborsa o'shani bajaradi.
 *
 * Ixtiyoriy argumentli key ([TransferFormKey]) bilan ishlash: forma 4 xil yo'l bilan ochiladi —
 * bo'sh (FAB), karta bilan (cross-tab), qabul qiluvchi bilan (deep link), ikkalasi (takrorlash).
 */
@HiltViewModel(assistedFactory = TransferFormViewModel.Factory::class)
class TransferFormViewModel @AssistedInject constructor(
    @Assisted private val navKey: TransferFormKey,
    private val observeCards: ObserveCardsUseCase,
    private val makeTransfer: MakeTransferUseCase,
) : BaseViewModel<Intent, UIState, Effect>(
    UIState(
        recipient = navKey.recipient.orEmpty(),
        prefilledRecipient = navKey.recipient.orEmpty(),
    ),
) {

    @AssistedFactory
    interface Factory {
        fun create(navKey: TransferFormKey): TransferFormViewModel
    }

    init {
        viewModelScope.launch {
            observeCards().collect { newCards ->
                setState {
                    val selected = selectedCard?.let { current -> newCards.firstOrNull { it.id == current.id } }
                        ?: newCards.firstOrNull { it.id == navKey.fromCardId }
                        ?: newCards.firstOrNull { !it.isBlocked }
                    copy(isLoading = false, cards = newCards, selectedCard = selected)
                }
            }
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.RecipientChanged -> setState {
                copy(recipient = intent.value.filter(Char::isDigit).take(16), error = null)
            }

            is Intent.AmountChanged -> setState {
                copy(amount = intent.value.filter(Char::isDigit).take(12), error = null)
            }

            Intent.SelectCardClicked -> sendEffect(Effect.OpenSelectCard(currentState.selectedCard?.id))

            is Intent.CardSelected -> setState {
                copy(selectedCard = cards.firstOrNull { it.id == intent.cardId } ?: selectedCard)
            }

            // Dialogga hech narsa uzatilmaydi: u shu ViewModel holatini o'zi o'qiydi
            Intent.ContinueClicked -> if (currentState.canContinue) sendEffect(Effect.OpenConfirm)

            // Tasdiqlash dialogidan keladi. Holat yana tekshiriladi: masalan, process death'dan
            // keyin dialog tiklangan, ammo forma maydonlari bo'sh bo'lishi mumkin.
            Intent.TransferConfirmed -> if (currentState.canContinue) submit()

            // Back'ni ushlab qolish: forma "iflos" bo'lsa avval so'raymiz
            Intent.BackClicked -> if (currentState.isDirty) {
                setState { copy(showDiscardDialog = true) }
            } else {
                sendEffect(Effect.NavigateBack)
            }

            Intent.DiscardConfirmed -> {
                setState { copy(showDiscardDialog = false) }
                sendEffect(Effect.NavigateBack)
            }

            Intent.DiscardDismissed -> setState { copy(showDiscardDialog = false) }
        }
    }

    private fun submit() {
        val state = currentState
        val card = state.selectedCard ?: return
        if (state.isSubmitting) return
        viewModelScope.launch {
            setState { copy(isSubmitting = true, error = null) }
            makeTransfer(card.id, state.recipient, state.amountValue)
                .onSuccess { transfer ->
                    setState { copy(isSubmitting = false) }
                    sendEffect(Effect.OpenSuccess(transferId = transfer.id, amount = transfer.amount))
                }
                .onFailure { error ->
                    setState { copy(isSubmitting = false, error = error.message ?: "Xatolik") }
                }
        }
    }
}
