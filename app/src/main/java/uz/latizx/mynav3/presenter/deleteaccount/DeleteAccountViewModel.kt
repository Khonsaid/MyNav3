package uz.latizx.mynav3.presenter.deleteaccount

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.repository.SecurityRepository
import uz.latizx.mynav3.domain.usecase.LogoutUseCase
import javax.inject.Inject

@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    private val securityRepository: SecurityRepository,
    private val logout: LogoutUseCase,
) : BaseViewModel<DeleteAccountScreenContract.Intent, DeleteAccountScreenContract.UIState, DeleteAccountScreenContract.Effect>(
    DeleteAccountScreenContract.UIState()
) {

    override fun handleIntent(intent: DeleteAccountScreenContract.Intent) {
        when (intent) {
            is DeleteAccountScreenContract.Intent.ReasonSelected -> setState { copy(selectedReason = intent.reason) }

            DeleteAccountScreenContract.Intent.ContinueClicked -> if (currentState.selectedReason != null) sendEffect(
                DeleteAccountScreenContract.Effect.OpenConfirmDialog
            )

            DeleteAccountScreenContract.Intent.DeleteConfirmed -> {
                val reason = currentState.selectedReason ?: return
                if (currentState.isDeleting) return
                viewModelScope.launch {
                    setState { copy(isDeleting = true) }
                    securityRepository.deleteAccount(reason)
                    logout() // sessiya bayrog'i o'chadi → keyingi ochilishda Splash Login'ga yuboradi
                    sendEffect(DeleteAccountScreenContract.Effect.AccountDeleted)
                }
            }
        }
    }
}