package uz.latizx.mynav3.presenter.settings.security.changepin

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.latizx.mynav3.core.mvi.CollectEffect
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.settings.security.pin.PinInput
import uz.latizx.mynav3.ui.theme.MyNav3Theme

/**
 * Uchala qadam uchun BITTA Route. [step] — qaysi maydonni ko'rsatish.
 *
 * Effektlarni har bir qadam yig'adi: ekranda faqat eng tepadagi qadam turadi, demak effektni
 * aynan u oladi. Navigatsiya amallari esa "mutlaq" (navigate(ChangePinNewKey), popUpTo(...)) —
 * qaysi qadam bajarishidan qat'i nazar natija bir xil.
 */
@Composable
fun ChangePinRoute(
    viewModel: ChangePinViewModel,
    step: ChangePinStep,
    onOpenNewStep: () -> Unit,
    onOpenConfirmStep: () -> Unit,
    onFinished: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            ChangePinScreenContract.Effect.OpenNewStep -> onOpenNewStep()
            ChangePinScreenContract.Effect.OpenConfirmStep -> onOpenConfirmStep()
            ChangePinScreenContract.Effect.Finished -> onFinished()
        }
    }

    ChangePinScreen(
        state = state,
        step = step,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun ChangePinScreen(
    state: ChangePinScreenContract.UIState,
    step: ChangePinStep,
    onIntent: (ChangePinScreenContract.Intent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val title = "PIN-kod (${step.ordinal + 1}/${ChangePinStep.entries.size})"
    Scaffold(topBar = { AppTopBar(title = title, onBack = onNavigateBack) }) { padding ->
        PinInput(
            title = step.title,
            pin = state.pinFor(step),
            error = state.error,
            isLoading = state.isLoading,
            onPinChange = { pin -> onIntent(ChangePinScreenContract.Intent.PinChanged(step, pin)) },
            modifier = Modifier.padding(padding),
        )
    }
}

@Preview
@Composable
private fun ChangePinScreenPreview() {
    MyNav3Theme {
        ChangePinScreen(
            state = ChangePinScreenContract.UIState(),
            step = ChangePinStep.NEW,
            onIntent = {},
            onNavigateBack = {})
    }
}