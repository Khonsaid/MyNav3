package uz.latizx.mynav3.presenter.notifications

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.latizx.mynav3.domain.model.CategorySettings
import uz.latizx.mynav3.domain.model.NotificationCategory
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.LoadingView
import uz.latizx.mynav3.presenter.common.SimpleListRow
import uz.latizx.mynav3.ui.theme.MyNav3Theme


@Composable
fun NotificationCategoryRoute(
    viewModel: NotificationCategoryViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    NotificationCategoryScreen(state = state, onIntent = viewModel::onIntent, onNavigateBack = onNavigateBack)
}

@Composable
fun NotificationCategoryScreen(
    state: NotificationCategoryScreenContract.UIState,
    onIntent: (NotificationCategoryScreenContract.Intent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(topBar = { AppTopBar(title = state.category.displayName, onBack = onNavigateBack) }) { padding ->
        val settings = state.settings
        if (settings == null) {
            LoadingView(Modifier.padding(padding))
            return@Scaffold
        }
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = state.category.description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
            SimpleListRow(
                title = "Push-bildirishnoma",
                trailing = { Switch(checked = settings.push, onCheckedChange = { onIntent(NotificationCategoryScreenContract.Intent.PushToggled(it)) }) },
            )
            HorizontalDivider()
            SimpleListRow(
                title = "SMS",
                trailing = { Switch(checked = settings.sms, onCheckedChange = { onIntent(NotificationCategoryScreenContract.Intent.SmsToggled(it)) }) },
            )
        }
    }
}

@Preview
@Composable
private fun NotificationCategoryScreenPreview() {
    MyNav3Theme {
        NotificationCategoryScreen(
            state = NotificationCategoryScreenContract.UIState(NotificationCategory.CARDS, CategorySettings(push = true, sms = false)),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}