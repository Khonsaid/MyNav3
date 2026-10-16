package uz.latizx.mynav3.presenter.deleteaccount

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.DialogCard
import uz.latizx.mynav3.presenter.common.SimpleListRow
import uz.latizx.mynav3.presenter.deleteaccount.DeleteAccountScreenContract.REASONS
import uz.latizx.mynav3.ui.theme.MyNav3Theme

@Composable
fun DeleteAccountRoute(
    viewModel: DeleteAccountViewModel,
    onOpenConfirmDialog: () -> Unit,
    onAccountDeleted: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            DeleteAccountScreenContract.Effect.OpenConfirmDialog -> onOpenConfirmDialog()
            DeleteAccountScreenContract.Effect.AccountDeleted -> onAccountDeleted()
        }
    }

    DeleteAccountScreen(state = state, onIntent = viewModel::onIntent, onNavigateBack = onNavigateBack)
}

@Composable
fun DeleteAccountScreen(
    state: DeleteAccountScreenContract.UIState,
    onIntent: (DeleteAccountScreenContract.Intent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(topBar = { AppTopBar(title = "Akkauntni o'chirish", onBack = onNavigateBack) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = "Nega ketmoqchisiz?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            REASONS.forEach { reason ->
                SimpleListRow(
                    title = reason,
                    enabled = !state.isDeleting,
                    leading = { RadioButton(selected = reason == state.selectedReason, onClick = null) },
                    onClick = { onIntent(DeleteAccountScreenContract.Intent.ReasonSelected(reason)) },
                )
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = dropUnlessResumed { onIntent(DeleteAccountScreenContract.Intent.ContinueClicked) },
                enabled = state.selectedReason != null && !state.isDeleting,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                Text(if (state.isDeleting) "O'chirilmoqda..." else "Davom etish")
            }
        }
    }
}

/** DIALOG MANZILI (holatsiz). Natija — `DeleteAccountConfirmed`. */
@Composable
fun DeleteAccountDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    DialogCard(
        title = "Akkaunt o'chirilsinmi?",
        confirmText = "O'chirish",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        destructive = true,
    ) {
        Text(
            text = "Bu amalni qaytarib bo'lmaydi. Barcha ma'lumotlaringiz o'chiriladi.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun DeleteAccountScreenPreview() {
    MyNav3Theme { DeleteAccountScreen(state = DeleteAccountScreenContract.UIState(selectedReason = REASONS[1]), onIntent = {}, onNavigateBack = {}) }
}