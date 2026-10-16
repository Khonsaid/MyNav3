package uz.latizx.mynav3.presenter.appearance

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : BaseViewModel<AppearanceScreenContract.Intent, AppearanceScreenContract.UIState, AppearanceScreenContract.Effect>(
    AppearanceScreenContract.UIState()
) {

    init {
        viewModelScope.launch {
            settingsRepository.observeTheme().collect { theme -> setState { copy(theme = theme) } }
        }
    }

    override fun handleIntent(intent: AppearanceScreenContract.Intent) {
        when (intent) {
            AppearanceScreenContract.Intent.ThemeClicked -> sendEffect(
                AppearanceScreenContract.Effect.OpenThemeSheet(
                    currentState.theme
                )
            )
            // Saqlanadi → MainActivity ham shu Flow'ni kuzatadi → butun ilova mavzusi o'zgaradi
            is AppearanceScreenContract.Intent.ThemeSelected -> viewModelScope.launch {
                settingsRepository.setTheme(
                    intent.theme
                )
            }
        }
    }
}