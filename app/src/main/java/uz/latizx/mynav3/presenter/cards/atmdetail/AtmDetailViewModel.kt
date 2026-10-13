package uz.latizx.mynav3.presenter.cards.atmdetail

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.usecase.GetAtmUseCase
import uz.latizx.mynav3.presenter.cards.atmdetail.AtmDetailScreenContract.Effect
import uz.latizx.mynav3.presenter.cards.atmdetail.AtmDetailScreenContract.Intent
import uz.latizx.mynav3.presenter.cards.atmdetail.AtmDetailScreenContract.UIState
import uz.latizx.mynav3.presenter.cards.navigation.AtmDetailKey

@HiltViewModel(assistedFactory = AtmDetailViewModel.Factory::class)
class AtmDetailViewModel @AssistedInject constructor(
    @Assisted private val navKey: AtmDetailKey,
    private val getAtm: GetAtmUseCase,
) : BaseViewModel<Intent, UIState, Effect>(UIState()) {

    @AssistedFactory
    interface Factory {
        fun create(navKey: AtmDetailKey): AtmDetailViewModel
    }

    init {
        load()
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.CloseClicked -> sendEffect(Effect.NavigateBack)
            Intent.RetryClicked -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            val atm = runCatching { getAtm(navKey.atmId) }.getOrNull()
            setState {
                copy(isLoading = false, atm = atm, error = if (atm == null) "Bankomat topilmadi" else null)
            }
        }
    }
}
