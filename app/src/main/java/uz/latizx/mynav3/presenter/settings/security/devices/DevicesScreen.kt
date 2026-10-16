package uz.latizx.mynav3.presenter.settings.security.devices

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import uz.latizx.mynav3.core.mvi.CollectEffect
import uz.latizx.mynav3.domain.model.Device
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.LoadingView
import uz.latizx.mynav3.presenter.common.SimpleListRow
import uz.latizx.mynav3.ui.theme.MyNav3Theme

@Composable
fun DevicesRoute(
    viewModel: DevicesViewModel,
    onOpenDevice: (deviceId: String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is DevicesScreenContract.Effect.OpenDevice -> onOpenDevice(effect.deviceId)
        }
    }

    DevicesScreen(state = state, onIntent = viewModel::onIntent, onNavigateBack = onNavigateBack)
}

@Composable
fun DevicesScreen(
    state: DevicesScreenContract.UIState,
    onIntent: (DevicesScreenContract.Intent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(topBar = { AppTopBar(title = "Faol qurilmalar", onBack = onNavigateBack) }) { padding ->
        if (state.isLoading) {
            LoadingView(Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(state.devices, key = Device::id) { device ->
                SimpleListRow(
                    title = device.name,
                    subtitle = device.lastActive,
                    onClick = dropUnlessResumed { onIntent(DevicesScreenContract.Intent.DeviceClicked(device.id)) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Preview
@Composable
private fun DevicesScreenPreview() {
    MyNav3Theme {
        DevicesScreen(
            state = DevicesScreenContract.UIState(isLoading = false, devices = listOf(Device("1", "Pixel 9", "Hozir", true))),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}