package uz.latizx.mynav3.presenter.settings.home

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class SettingsHomeViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : BaseViewModel<SettingsHomeScreenContract.Intent, SettingsHomeScreenContract.UIState, SettingsHomeScreenContract.Effect>(
    SettingsHomeScreenContract.UIState()
) {

    init {
        viewModelScope.launch {
            settingsRepository.observeTheme().collect { theme -> setState { copy(theme = theme) } }
        }
    }

    override fun handleIntent(intent: SettingsHomeScreenContract.Intent) {
        when (intent) {
            is SettingsHomeScreenContract.Intent.SectionClicked -> sendEffect(
                SettingsHomeScreenContract.Effect.OpenSection(intent.section)
            )
        }
    }
}