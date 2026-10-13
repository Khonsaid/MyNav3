package uz.latizx.mynav3.presenter.cards.atm

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import uz.latizx.mynav3.R
import uz.latizx.mynav3.core.mvi.CollectEffect
import uz.latizx.mynav3.domain.model.Atm
import uz.latizx.mynav3.presenter.cards.atm.CardATMScreenContract.Effect
import uz.latizx.mynav3.presenter.cards.atm.CardATMScreenContract.Intent
import uz.latizx.mynav3.presenter.cards.atm.CardATMScreenContract.UIState
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.EmptyView
import uz.latizx.mynav3.presenter.common.ErrorView
import uz.latizx.mynav3.presenter.common.LoadingView
import uz.latizx.mynav3.presenter.common.PreviewData
import uz.latizx.mynav3.presenter.common.SimpleListRow
import uz.latizx.mynav3.presenter.common.formatDistance
import uz.latizx.mynav3.presenter.common.maskCardNumber
import uz.latizx.mynav3.ui.theme.MyNav3Theme

/** ROUTE: ViewModel + Effect → navigatsiya callback'lari. */
@Composable
fun CardATMRoute(
    viewModel: CardATMViewModel,
    onNavigateBack: () -> Unit,
    onOpenAtmDetail: (atmId: String) -> Unit,
    onBackToCards: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            Effect.NavigateBack -> onNavigateBack()
            is Effect.OpenAtmDetail -> onOpenAtmDetail(effect.atmId)
            Effect.BackToCards -> onBackToCards()
        }
    }

    CardATMScreen(state = state, onIntent = viewModel::onIntent)
}

/** SCREEN: faqat [UIState] → UI, foydalanuvchi harakatlari → [Intent]. */
@Composable
fun CardATMScreen(
    state: UIState,
    onIntent: (Intent) -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Bankomatlar",
                onBack = dropUnlessResumed { onIntent(Intent.BackClicked) },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingView(Modifier.padding(padding))

            state.error != null -> ErrorView(
                message = state.error,
                modifier = Modifier.padding(padding),
                onRetry = { onIntent(Intent.RetryClicked) },
            )

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                state.card?.let { card ->
                    Text(
                        text = "${card.type.title} • ${maskCardNumber(card.number)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                FilterRow(selected = state.filter, onSelect = { filter -> onIntent(Intent.FilterSelected(filter)) })
                AtmList(
                    atms = state.visibleAtms,
                    onAtmClick = { atmId -> onIntent(Intent.AtmClicked(atmId)) },
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(
                    // popUpTo(CardsKey): CardDetail va shu ekran birdaniga yopiladi
                    onClick = dropUnlessResumed { onIntent(Intent.BackToCardsClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) { Text("Kartalar ro'yxatiga qaytish") }
            }
        }
    }
}

@Composable
private fun FilterRow(
    selected: AtmFilter,
    onSelect: (AtmFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AtmFilter.entries.forEach { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                label = { Text(filter.title) },
            )
        }
    }
}

@Composable
private fun AtmList(
    atms: List<Atm>,
    onAtmClick: (atmId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (atms.isEmpty()) {
        EmptyView(text = "Bu filtr bo'yicha bankomat topilmadi", modifier = modifier)
        return
    }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(vertical = 8.dp)) {
        items(items = atms, key = { atm -> atm.id }) { atm ->
            SimpleListRow(
                title = atm.name,
                subtitle = "${atm.address} • ${atm.workingHours}",
                trailingText = formatDistance(atm.distanceMeters),
                leading = {
                    Icon(
                        painter = painterResource(R.drawable.ic_location),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                onClick = dropUnlessResumed { onAtmClick(atm.id) },
            )
            HorizontalDivider()
        }
    }
}

@Preview
@Composable
private fun CardATMScreenPreview() {
    MyNav3Theme {
        CardATMScreen(
            state = UIState(isLoading = false, card = PreviewData.cards.first(), atms = PreviewData.atms),
            onIntent = {},
        )
    }
}
