package uz.latizx.mynav3.presenter.appearance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import uz.latizx.mynav3.core.mvi.CollectEffect
import uz.latizx.mynav3.domain.model.AppTheme
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.SimpleListRow
import uz.latizx.mynav3.ui.theme.MyNav3Theme

@Composable
fun AppearanceRoute(
    viewModel: AppearanceViewModel,
    onOpenThemeSheet: (current: AppTheme) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is AppearanceScreenContract.Effect.OpenThemeSheet -> onOpenThemeSheet(effect.current)
        }
    }

    AppearanceScreen(state = state, onIntent = viewModel::onIntent, onNavigateBack = onNavigateBack)
}

@Composable
fun AppearanceScreen(
    state: AppearanceScreenContract.UIState,
    onIntent: (AppearanceScreenContract.Intent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(topBar = { AppTopBar(title = "Ko'rinish", onBack = onNavigateBack) }) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            SimpleListRow(
                title = "Mavzu",
                subtitle = state.theme.displayName,
                onClick = dropUnlessResumed { onIntent(AppearanceScreenContract.Intent.ThemeClicked) },
            )
        }
    }
}

@Preview
@Composable
private fun AppearanceScreenPreview() {
    MyNav3Theme {
        AppearanceScreen(
            state = AppearanceScreenContract.UIState(),
            onIntent = {},
            onNavigateBack = {})
    }
}