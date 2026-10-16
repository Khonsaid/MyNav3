package uz.latizx.mynav3.presenter.notifications

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import uz.latizx.mynav3.core.mvi.CollectEffect
import uz.latizx.mynav3.domain.model.CategorySettings
import uz.latizx.mynav3.domain.model.NotificationCategory
import uz.latizx.mynav3.domain.model.NotificationSettings
import uz.latizx.mynav3.domain.model.QuietHours
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.LoadingView
import uz.latizx.mynav3.presenter.common.SimpleListRow
import uz.latizx.mynav3.ui.theme.MyNav3Theme

@Composable
fun NotificationsRoute(
    viewModel: NotificationsViewModel,
    onOpenCategory: (NotificationCategory) -> Unit,
    onOpenQuietHours: (fromHour: Int, toHour: Int) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is NotificationsScreenContract.Effect.OpenCategory -> onOpenCategory(effect.category)
            is NotificationsScreenContract.Effect.OpenQuietHours -> onOpenQuietHours(effect.fromHour, effect.toHour)
        }
    }

    NotificationsScreen(state = state, onIntent = viewModel::onIntent, onNavigateBack = onNavigateBack)
}

@Composable
fun NotificationsScreen(
    state: NotificationsScreenContract.UIState,
    onIntent: (NotificationsScreenContract.Intent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(topBar = { AppTopBar(title = "Bildirishnomalar", onBack = onNavigateBack) }) { padding ->
        val settings = state.settings
        if (state.isLoading || settings == null) {
            LoadingView(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            NotificationCategory.entries.forEach { category ->
                val value = settings.categories[category]
                SimpleListRow(
                    title = category.displayName,
                    subtitle = value?.summary(),
                    onClick = dropUnlessResumed { onIntent(NotificationsScreenContract.Intent.CategoryClicked(category)) },
                )
                HorizontalDivider()
            }
            val quiet = settings.quietHours
            SimpleListRow(
                title = "Jim soatlar",
                subtitle = "%02d:00 – %02d:00".format(quiet.fromHour, quiet.toHour),
                onClick = dropUnlessResumed { onIntent(NotificationsScreenContract.Intent.QuietHoursClicked) },
                trailing = {
                    Switch(
                        checked = quiet.enabled,
                        onCheckedChange = { onIntent(NotificationsScreenContract.Intent.QuietHoursToggled(it)) },
                    )
                },
            )
        }
    }
}

private fun CategorySettings.summary(): String = buildList {
    if (push) add("Push")
    if (sms) add("SMS")
}.joinToString().ifEmpty { "O'chirilgan" }

@Preview
@Composable
private fun NotificationsScreenPreview() {
    MyNav3Theme {
        NotificationsScreen(
            state = NotificationsScreenContract.UIState(
                isLoading = false,
                settings = NotificationSettings(
                    categories = NotificationCategory.entries.associateWith {
                        CategorySettings(
                            true,
                            false
                        )
                    },
                    quietHours = QuietHours(false, 23, 7),
                ),
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}