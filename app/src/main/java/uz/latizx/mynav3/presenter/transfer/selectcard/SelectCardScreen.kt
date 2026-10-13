package uz.latizx.mynav3.presenter.transfer.selectcard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import uz.latizx.mynav3.core.mvi.CollectEffect
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.LoadingView
import uz.latizx.mynav3.presenter.common.PreviewData
import uz.latizx.mynav3.presenter.common.SimpleListRow
import uz.latizx.mynav3.presenter.common.formatSum
import uz.latizx.mynav3.presenter.common.maskCardNumber
import uz.latizx.mynav3.presenter.transfer.selectcard.SelectCardScreenContract.Effect
import uz.latizx.mynav3.presenter.transfer.selectcard.SelectCardScreenContract.Intent
import uz.latizx.mynav3.presenter.transfer.selectcard.SelectCardScreenContract.UIState
import uz.latizx.mynav3.ui.theme.MyNav3Theme

@Composable
fun SelectCardRoute(
    viewModel: SelectCardViewModel,
    onCardChosen: (cardId: String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is Effect.CardChosen -> onCardChosen(effect.cardId)
            Effect.NavigateBack -> onNavigateBack()
        }
    }

    SelectCardScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun SelectCardScreen(
    state: UIState,
    onIntent: (Intent) -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Kartani tanlang",
                onBack = dropUnlessResumed { onIntent(Intent.BackClicked) },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingView(Modifier.padding(padding))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = padding) {
                items(items = state.cards, key = { card -> card.id }) { card ->
                    SimpleListRow(
                        title = "${card.type.title} • ${maskCardNumber(card.number)}",
                        subtitle = if (card.isBlocked) "Bloklangan" else formatSum(card.balance),
                        enabled = !card.isBlocked,
                        leading = {
                            RadioButton(selected = card.id == state.selectedCardId, onClick = null)
                        },
                        onClick = dropUnlessResumed { onIntent(Intent.CardClicked(card.id)) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview
@Composable
private fun SelectCardScreenPreview() {
    MyNav3Theme {
        SelectCardScreen(
            state = UIState(isLoading = false, cards = PreviewData.cards, selectedCardId = "card-1"),
            onIntent = {},
        )
    }
}
