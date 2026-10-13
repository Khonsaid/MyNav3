package uz.latizx.mynav3.presenter.transfer.history

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.usecase.ObserveTransfersUseCase
import uz.latizx.mynav3.presenter.transfer.history.TransfersScreenContract.Effect
import uz.latizx.mynav3.presenter.transfer.history.TransfersScreenContract.Intent
import uz.latizx.mynav3.presenter.transfer.history.TransfersScreenContract.UIState

@HiltViewModel
class TransfersViewModel @Inject constructor(
    private val observeTransfers: ObserveTransfersUseCase,
) : BaseViewModel<Intent, UIState, Effect>(UIState()) {

    init {
        viewModelScope.launch {
            observeTransfers().collect { transfers ->
                setState { copy(isLoading = false, transfers = transfers) }
            }
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.NewTransferClicked -> sendEffect(Effect.OpenNewTransfer)
            is Intent.RepeatClicked -> sendEffect(
                Effect.OpenRepeatTransfer(
                    fromCardId = intent.transfer.fromCardId,
                    recipient = intent.transfer.recipient,
                ),
            )
        }
    }
}
