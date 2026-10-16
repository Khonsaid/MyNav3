package uz.latizx.mynav3.presenter.notifications

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.repository.SettingsRepository
import uz.latizx.mynav3.presenter.settings.navigation.NotificationCategoryKey

@HiltViewModel(assistedFactory = NotificationCategoryViewModel.Factory::class)
class NotificationCategoryViewModel @AssistedInject constructor(
    @Assisted navKey: NotificationCategoryKey,
    private val settingsRepository: SettingsRepository,
) : BaseViewModel<NotificationCategoryScreenContract.Intent, NotificationCategoryScreenContract.UIState, NotificationCategoryScreenContract.Effect>(
    NotificationCategoryScreenContract.UIState(category = navKey.category)
) {

    @AssistedFactory
    interface Factory {
        fun create(navKey: NotificationCategoryKey): NotificationCategoryViewModel
    }

    init {
        viewModelScope.launch {
            settingsRepository.observeNotifications().collect { all ->
                setState { copy(settings = all.categories[category]) }
            }
        }
    }

    override fun handleIntent(intent: NotificationCategoryScreenContract.Intent) {
        val current = currentState.settings ?: return
        val updated = when (intent) {
            is NotificationCategoryScreenContract.Intent.PushToggled -> current.copy(push = intent.enabled)
            is NotificationCategoryScreenContract.Intent.SmsToggled -> current.copy(sms = intent.enabled)
        }
        viewModelScope.launch { settingsRepository.setCategory(currentState.category, updated) }
    }
}