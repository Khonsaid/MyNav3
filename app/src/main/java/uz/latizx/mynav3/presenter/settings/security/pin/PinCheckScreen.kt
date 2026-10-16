package uz.latizx.mynav3.presenter.settings.security.pin

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.latizx.mynav3.core.mvi.CollectEffect
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.ui.theme.MyNav3Theme

@Composable
fun PinCheckRoute(
    viewModel: PinCheckViewModel,
    onUnlocked: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            PinCheckScreenContract.Effect.Unlocked -> onUnlocked()
        }
    }

    PinCheckScreen(state = state, onIntent = viewModel::onIntent, onNavigateBack = onNavigateBack)
}

@Composable
fun PinCheckScreen(
    state: PinCheckScreenContract.UIState,
    onIntent: (PinCheckScreenContract.Intent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(topBar = { AppTopBar(title = "Xavfsizlik", onBack = onNavigateBack) }) { padding ->
        PinInput(
            title = "PIN-kodni kiriting",
            pin = state.pin,
            error = state.error,
            isLoading = state.isChecking,
            onPinChange = { onIntent(PinCheckScreenContract.Intent.PinChanged(it)) },
            hint = "Demo PIN: 0000",
            modifier = Modifier.padding(padding),
        )
    }
}

@Preview
@Composable
private fun PinCheckScreenPreview() {
    MyNav3Theme { PinCheckScreen(state = PinCheckScreenContract.UIState(), onIntent = {}, onNavigateBack = {}) }
}