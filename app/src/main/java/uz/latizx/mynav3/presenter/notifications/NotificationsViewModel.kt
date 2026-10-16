package uz.latizx.mynav3.presenter.notifications

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import uz.latizx.mynav3.core.mvi.BaseViewModel
import uz.latizx.mynav3.domain.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : BaseViewModel<NotificationsScreenContract.Intent, NotificationsScreenContract.UIState, NotificationsScreenContract.Effect>(
    NotificationsScreenContract.UIState()
) {

    init {
        // Kategoriya detalida o'zgartirilgan qiymat bu ekranga Flow orqali o'zi keladi —
        // natija (ResultEventBus) kerak emas
        viewModelScope.launch {
            settingsRepository.observeNotifications().collect { settings ->
                setState { copy(isLoading = false, settings = settings) }
            }
        }
    }

    override fun handleIntent(intent: NotificationsScreenContract.Intent) {
        val quietHours = currentState.settings?.quietHours ?: return
        when (intent) {
            is NotificationsScreenContract.Intent.CategoryClicked -> sendEffect(
                NotificationsScreenContract.Effect.OpenCategory(intent.category)
            )

            is NotificationsScreenContract.Intent.QuietHoursToggled -> viewModelScope.launch {
                settingsRepository.setQuietHours(quietHours.copy(enabled = intent.enabled))
            }

            NotificationsScreenContract.Intent.QuietHoursClicked ->
                sendEffect(
                    NotificationsScreenContract.Effect.OpenQuietHours(
                        quietHours.fromHour,
                        quietHours.toHour
                    )
                )

            is NotificationsScreenContract.Intent.QuietHoursSelected -> viewModelScope.launch {
                settingsRepository.setQuietHours(
                    quietHours.copy(
                        enabled = true,
                        fromHour = intent.fromHour,
                        toHour = intent.toHour
                    ),
                )
            }
        }
    }
}