package uz.latizx.mynav3.presenter.cards.detail

import android.R.attr.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import uz.latizx.mynav3.core.navigation.navSharedElement
import uz.latizx.mynav3.domain.model.Card
import uz.latizx.mynav3.presenter.cards.detail.CardDetailScreenContract.Effect
import uz.latizx.mynav3.presenter.cards.detail.CardDetailScreenContract.Intent
import uz.latizx.mynav3.presenter.cards.detail.CardDetailScreenContract.UIState
import uz.latizx.mynav3.ui.theme.MyNav3Theme
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.BankCardView
import uz.latizx.mynav3.presenter.common.ErrorView
import uz.latizx.mynav3.presenter.common.LoadingView
import uz.latizx.mynav3.presenter.common.PreviewData
import uz.latizx.mynav3.presenter.common.bankCardSharedKey

@Composable
fun CardDetailRoute(
    viewModel: CardDetailViewModel,
    onNavigateBack: () -> Unit,
    onOpenAtms: (cardId: String) -> Unit,
    onOpenTransfer: (cardId: String) -> Unit,
    onOpenBlockDialog: (cardId: String, block: Boolean) -> Unit,
    onOpenActions: (cardId: String, isBlocked: Boolean) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            Effect.NavigateBack -> onNavigateBack()
            is Effect.OpenAtms -> onOpenAtms(effect.cardId)
            is Effect.OpenTransfer -> onOpenTransfer(effect.cardId)
            is Effect.OpenBlockDialog -> onOpenBlockDialog(effect.cardId, effect.block)
            is Effect.OpenActions -> onOpenActions(effect.cardId, effect.isBlocked)
        }
    }

    CardDetailScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun CardDetailScreen(
    state: UIState,
    onIntent: (Intent) -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Karta",
                onBack = dropUnlessResumed { onIntent(Intent.BackClicked) },
                actions = {
                    // Bottom sheet MANZILINI ochadi (CardActionsSheetKey)
                    IconButton(onClick = dropUnlessResumed { onIntent(Intent.MoreClicked) }) {
                        Icon(painter = painterResource(R.drawable.ic_more_vert), contentDescription = "Amallar")
                    }
                },
            )
        },
    ) { padding ->
        val card = state.card
        when {
            state.isLoading && card == null -> LoadingView(Modifier.padding(padding))

            card == null -> ErrorView(
                message = state.error ?: "Karta topilmadi",
                modifier = Modifier.padding(padding),
                onRetry = { onIntent(Intent.RetryClicked) },
            )

            else -> CardDetailContent(
                card = card,
                isUpdating = state.isUpdating,
                onIntent = onIntent,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun CardDetailContent(
    card: Card,
    isUpdating: Boolean,
    onIntent: (Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // SHARED ELEMENT: ro'yxatdagi karta bilan bir xil kalit → karta shu joyga "uchib" keladi.
        // Karta birinchi kadrdanoq ko'rinishi muhim (ViewModel uni sinxron oladi), aks holda
        // o'tish tugaguncha nishon bo'lmaydi va uchish sodir bo'lmaydi.
        BankCardView(
            card = card,
            modifier = Modifier.navSharedElement(bankCardSharedKey(card.id)),
        )
        Spacer(Modifier.height(4.dp))

        // Oddiy push: CardATMKey
        Button(
            onClick = dropUnlessResumed { onIntent(Intent.AtmsClicked) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Bu karta ishlaydigan bankomatlar") }

        // Cross-tab: O'tkazmalar tabiga o'tib, formani shu karta bilan ochadi
        FilledTonalButton(
            onClick = dropUnlessResumed { onIntent(Intent.TransferClicked) },
            enabled = !card.isBlocked,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Shu kartadan o'tkazma") }

        // Dialog manzili + natija
        OutlinedButton(
            onClick = dropUnlessResumed { onIntent(Intent.BlockToggleClicked) },
            enabled = !isUpdating,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isUpdating) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(if (card.isBlocked) "Blokdan chiqarish" else "Kartani bloklash")
            }
        }
    }
}

@Preview
@Composable
private fun CardDetailScreenPreview() {
    MyNav3Theme {
        CardDetailScreen(
            state = UIState(isLoading = false, card = PreviewData.cards.first()),
            onIntent = {},
        )
    }
}
