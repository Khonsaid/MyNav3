package uz.latizx.mynav3.presenter.settings.security

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.LoadingView
import uz.latizx.mynav3.presenter.common.SimpleListRow
import uz.latizx.mynav3.ui.theme.MyNav3Theme

@Composable
fun SecurityRoute(
    viewModel: SecurityViewModel,
    onRequirePin: () -> Unit,
    onOpenChangePin: () -> Unit,
    onOpenDevices: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            SecurityScreenContract.Effect.RequirePin -> onRequirePin()
            SecurityScreenContract.Effect.OpenChangePin -> onOpenChangePin()
            SecurityScreenContract.Effect.OpenDevices -> onOpenDevices()
        }
    }

    SecurityScreen(state = state, onIntent = viewModel::onIntent, onNavigateBack = onNavigateBack)
}


@Composable
fun SecurityScreen(
    state: SecurityScreenContract.UIState,
    onIntent: (SecurityScreenContract.Intent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(topBar = { AppTopBar(title = "Xavfsizlik", onBack = onNavigateBack) }) { padding ->
        if (!state.isUnlocked) {
            // Bir lahzaga ko'rinadi — keyin PIN darvozasi bilan almashtiriladi
            LoadingView(Modifier.padding(padding))
            return@Scaffold
        }
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SimpleListRow(
                title = "Barmoq izi bilan kirish",
                trailing = {
                    Switch(
                        checked = state.biometricEnabled,
                        onCheckedChange = { onIntent(SecurityScreenContract.Intent.BiometricToggled(it)) },
                    )
                },
            )
            HorizontalDivider()
            SimpleListRow(
                title = "PIN-kodni o'zgartirish",
                subtitle = "3 qadam: joriy → yangi → tasdiqlash",
                onClick = dropUnlessResumed { onIntent(SecurityScreenContract.Intent.ChangePinClicked) },
            )
            HorizontalDivider()
            SimpleListRow(
                title = "Faol qurilmalar",
                onClick = dropUnlessResumed { onIntent(SecurityScreenContract.Intent.DevicesClicked) },
            )
        }
    }
}

@Preview
@Composable
private fun SecurityScreenPreview() {
    MyNav3Theme {
        SecurityScreen(state = SecurityScreenContract.UIState(isUnlocked = true), onIntent = {}, onNavigateBack = {})
    }
}

