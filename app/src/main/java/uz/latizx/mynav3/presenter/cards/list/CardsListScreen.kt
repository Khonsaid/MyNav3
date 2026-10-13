package uz.latizx.mynav3.presenter.cards.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import uz.latizx.mynav3.core.navigation.navSharedElement
import uz.latizx.mynav3.presenter.cards.list.CardsListScreenContract.Effect
import uz.latizx.mynav3.presenter.cards.list.CardsListScreenContract.Intent
import uz.latizx.mynav3.presenter.cards.list.CardsListScreenContract.UIState
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.BankCardView
import uz.latizx.mynav3.presenter.common.ErrorView
import uz.latizx.mynav3.presenter.common.LoadingView
import uz.latizx.mynav3.presenter.common.PreviewData
import uz.latizx.mynav3.presenter.common.bankCardSharedKey
import uz.latizx.mynav3.presenter.common.formatSum
import uz.latizx.mynav3.ui.theme.MyNav3Theme

@Composable
fun CardsListRoute(
    viewModel: CardsListViewModel,
    onOpenCard: (cardId: String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is Effect.OpenCardDetail -> onOpenCard(effect.cardId)
        }
    }

    CardsListScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun CardsListScreen(
    state: UIState,
    onIntent: (Intent) -> Unit,
) {
    Scaffold(topBar = { AppTopBar(title = "Kartalarim") }) { padding ->
        when {
            state.isLoading && state.cards.isEmpty() -> LoadingView(Modifier.padding(padding))

            state.error != null -> ErrorView(
                message = state.error,
                modifier = Modifier.padding(padding),
                onRetry = { onIntent(Intent.RetryClicked) },
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Column {
                        Text(
                            text = "Umumiy balans",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = formatSum(state.totalBalance),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                items(items = state.cards, key = { card -> card.id }) { card ->
                    BankCardView(
                        card = card,
                        // SHARED ELEMENT: detal ekranidagi karta bilan bir xil kalit → karta
                        // ro'yxatdan detal ekraniga "uchib" o'tadi (Back'da — qaytib keladi)
                        modifier = Modifier.navSharedElement(bankCardSharedKey(card.id)),
                        // Animatsiya paytidagi ikkinchi bosish navigatsiyani takrorlamaydi
                        onClick = dropUnlessResumed { onIntent(Intent.CardClicked(card.id)) },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CardsListScreenPreview() {
    MyNav3Theme {
        CardsListScreen(
            state = UIState(isLoading = false, cards = PreviewData.cards),
            onIntent = {},
        )
    }
}
