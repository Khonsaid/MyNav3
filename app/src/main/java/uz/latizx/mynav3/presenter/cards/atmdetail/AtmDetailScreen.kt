package uz.latizx.mynav3.presenter.cards.atmdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import uz.latizx.mynav3.core.mvi.CollectEffect
import uz.latizx.mynav3.domain.model.Atm
import uz.latizx.mynav3.presenter.cards.atmdetail.AtmDetailScreenContract.Effect
import uz.latizx.mynav3.presenter.cards.atmdetail.AtmDetailScreenContract.Intent
import uz.latizx.mynav3.presenter.cards.atmdetail.AtmDetailScreenContract.UIState
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.ErrorView
import uz.latizx.mynav3.presenter.common.LoadingView
import uz.latizx.mynav3.presenter.common.PreviewData
import uz.latizx.mynav3.presenter.common.formatDistance
import uz.latizx.mynav3.ui.theme.MyNav3Theme

@Composable
fun AtmDetailRoute(
    viewModel: AtmDetailViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            Effect.NavigateBack -> onNavigateBack()
        }
    }

    AtmDetailScreen(state = state, onIntent = viewModel::onIntent)
}

/** Bu ekran pastdan yuqoriga chiqadi: `entry<AtmDetailKey>(metadata = NavTransitions.slideUp())`. */
@Composable
fun AtmDetailScreen(
    state: UIState,
    onIntent: (Intent) -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Bankomat",
                onBack = dropUnlessResumed { onIntent(Intent.CloseClicked) },
            )
        },
    ) { padding ->
        val atm = state.atm
        when {
            state.isLoading -> LoadingView(Modifier.padding(padding))

            atm == null -> ErrorView(
                message = state.error ?: "Bankomat topilmadi",
                modifier = Modifier.padding(padding),
                onRetry = { onIntent(Intent.RetryClicked) },
            )

            else -> AtmDetailContent(
                atm = atm,
                onClose = { onIntent(Intent.CloseClicked) },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun AtmDetailContent(
    atm: Atm,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = atm.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        InfoLine(label = "Manzil", value = atm.address)
        InfoLine(label = "Ish vaqti", value = atm.workingHours)
        InfoLine(label = "Masofa", value = formatDistance(atm.distanceMeters))
        InfoLine(label = "Naqd pul qabul qilish", value = if (atm.supportsCashIn) "Ha" else "Yo'q")
        InfoLine(label = "Kartalar", value = atm.supportedCardTypes.joinToString { type -> type.title })
        Spacer(Modifier.weight(1f))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Yopish") }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview
@Composable
private fun AtmDetailScreenPreview() {
    MyNav3Theme {
        AtmDetailScreen(state = UIState(isLoading = false, atm = PreviewData.atms.first()), onIntent = {})
    }
}
