package uz.latizx.mynav3.presenter.settings.security.devices

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import uz.latizx.mynav3.core.mvi.CollectEffect
import uz.latizx.mynav3.domain.model.Device
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.EmptyView
import uz.latizx.mynav3.presenter.common.LoadingView
import uz.latizx.mynav3.ui.theme.MyNav3Theme

@Composable
fun DeviceDetailRoute(
    viewModel: DeviceDetailViewModel,
    onOpenTerminateDialog: (deviceName: String) -> Unit,
    onTerminated: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is DeviceDetailScreenContract.Effect.OpenTerminateDialog -> onOpenTerminateDialog(effect.deviceName)
            DeviceDetailScreenContract.Effect.Terminated -> onTerminated()
        }
    }

    DeviceDetailScreen(state = state, onIntent = viewModel::onIntent, onNavigateBack = onNavigateBack)
}

@Composable
fun DeviceDetailScreen(
    state:DeviceDetailScreenContract. UIState,
    onIntent: (DeviceDetailScreenContract.Intent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(topBar = { AppTopBar(title = "Qurilma", onBack = onNavigateBack) }) { padding ->
        val device = state.device
        when {
            state.isLoading -> LoadingView(Modifier.padding(padding))
            device == null -> EmptyView("Qurilma topilmadi", Modifier.padding(padding))
            else -> Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text(text = device.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Oxirgi faollik: ${device.lastActive}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = dropUnlessResumed { onIntent(DeviceDetailScreenContract.Intent.TerminateClicked) },
                    // O'zingiz turgan qurilmani chiqarib bo'lmaydi
                    enabled = !device.isCurrent && !state.isTerminating,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (state.isTerminating) "Chiqarilmoqda..." else "Qurilmani chiqarish")
                }
            }
        }
    }
}

@Preview
@Composable
private fun DeviceDetailScreenPreview() {
    MyNav3Theme {
        DeviceDetailScreen(
            state = DeviceDetailScreenContract.UIState(isLoading = false, device = Device(
                "2",
                "Galaxy A54",
                "Kecha",
                false
            )
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}