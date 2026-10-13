package uz.latizx.mynav3.presenter.transfer.history

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.EmptyView
import uz.latizx.mynav3.presenter.common.LoadingView
import uz.latizx.mynav3.presenter.common.PreviewData
import uz.latizx.mynav3.presenter.common.SimpleListRow
import uz.latizx.mynav3.presenter.common.formatDateTime
import uz.latizx.mynav3.presenter.common.formatSum
import uz.latizx.mynav3.presenter.transfer.history.TransfersScreenContract.Effect
import uz.latizx.mynav3.presenter.transfer.history.TransfersScreenContract.Intent
import uz.latizx.mynav3.presenter.transfer.history.TransfersScreenContract.UIState
import uz.latizx.mynav3.ui.theme.MyNav3Theme

@Composable
fun TransfersRoute(
    viewModel: TransfersViewModel,
    onOpenNewTransfer: () -> Unit,
    onOpenRepeatTransfer: (fromCardId: String, recipient: String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            Effect.OpenNewTransfer -> onOpenNewTransfer()
            is Effect.OpenRepeatTransfer -> onOpenRepeatTransfer(effect.fromCardId, effect.recipient)
        }
    }

    TransfersScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun TransfersScreen(
    state: UIState,
    onIntent: (Intent) -> Unit,
) {
    Scaffold(
        topBar = { AppTopBar(title = "O'tkazmalar") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = dropUnlessResumed { onIntent(Intent.NewTransferClicked) },
                icon = { Icon(painter = painterResource(R.drawable.ic_add), contentDescription = null) },
                text = { Text("Yangi o'tkazma") },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingView(Modifier.padding(padding))
            state.transfers.isEmpty() -> EmptyView("Hali o'tkazmalar yo'q", Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 88.dp,
                ),
            ) {
                items(items = state.transfers, key = { transfer -> transfer.id }) { transfer ->
                    SimpleListRow(
                        title = "→ ${transfer.recipient}",
                        subtitle = "${transfer.fromCardMasked} • ${formatDateTime(transfer.createdAtMillis)}",
                        trailingText = "-" + formatSum(transfer.amount),
                        trailing = {
                            TextButton(onClick = dropUnlessResumed { onIntent(Intent.RepeatClicked(transfer)) }) {
                                Text("Takrorlash")
                            }
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview
@Composable
private fun TransfersScreenPreview() {
    MyNav3Theme {
        TransfersScreen(state = UIState(isLoading = false, transfers = PreviewData.transfers), onIntent = {})
    }
}
