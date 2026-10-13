package uz.latizx.mynav3.presenter.transfer.form

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import uz.latizx.mynav3.core.mvi.CollectEffect
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.LoadingView
import uz.latizx.mynav3.presenter.common.PreviewData
import uz.latizx.mynav3.presenter.common.SimpleListRow
import uz.latizx.mynav3.presenter.common.formatSum
import uz.latizx.mynav3.presenter.common.maskCardNumber
import uz.latizx.mynav3.presenter.transfer.form.TransferFormScreenContract.Effect
import uz.latizx.mynav3.presenter.transfer.form.TransferFormScreenContract.Intent
import uz.latizx.mynav3.presenter.transfer.form.TransferFormScreenContract.UIState
import uz.latizx.mynav3.ui.theme.MyNav3Theme

@Composable
fun TransferFormRoute(
    viewModel: TransferFormViewModel,
    onNavigateBack: () -> Unit,
    onOpenSelectCard: (selectedCardId: String?) -> Unit,
    onOpenConfirm: () -> Unit,
    onTransferSuccess: (transferId: String, amount: Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Effect'larni FAQAT forma (parent) yig'adi. Child'lar (kartani tanlash, tasdiqlash) shu
    // ViewModel'ga faqat Intent yuboradi — bir martalik effektni ikki joy "talashmaydi".
    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is Effect.OpenSelectCard -> onOpenSelectCard(effect.selectedCardId)
            Effect.OpenConfirm -> onOpenConfirm()
            is Effect.OpenSuccess -> onTransferSuccess(effect.transferId, effect.amount)
            Effect.NavigateBack -> onNavigateBack()
        }
    }

    TransferFormScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun TransferFormScreen(
    state: UIState,
    onIntent: (Intent) -> Unit,
) {
    // BACK'NI USHLAB QOLISH: forma to'ldirilgan bo'lsa tizim Back'i (va predictive back)
    // to'g'ridan-to'g'ri NavDisplay'ga bormaydi — avval "o'zgarishlar yo'qolsinmi?" so'raladi.
    // Forma bo'sh bo'lsa (enabled = false) Back odatdagidek ishlaydi.
    BackHandler(enabled = state.isDirty) { onIntent(Intent.BackClicked) }

    // Bu dialog — navigatsiya MANZILI EMAS, oddiy UI holati (UIState.showDiscardDialog).
    // Qachon qaysi biri? Back stack'da turishi, natija qaytarishi yoki process death'dan keyin
    // tiklanishi kerak bo'lsa → manzil (DialogSceneStrategy). Shunchaki tasdiqlash → UI holat.
    if (state.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { onIntent(Intent.DiscardDismissed) },
            title = { Text("O'zgarishlar saqlanmaydi") },
            text = { Text("Kiritilgan ma'lumotlar o'chib ketadi. Chiqasizmi?") },
            confirmButton = {
                TextButton(onClick = { onIntent(Intent.DiscardConfirmed) }) { Text("Chiqish") }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(Intent.DiscardDismissed) }) { Text("Qolish") }
            },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Yangi o'tkazma",
                onBack = dropUnlessResumed { onIntent(Intent.BackClicked) },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingView(Modifier.padding(padding))
        } else {
            TransferFormContent(
                state = state,
                onIntent = onIntent,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun TransferFormContent(
    state: UIState,
    onIntent: (Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorText = state.error
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Qaysi kartadan", style = MaterialTheme.typography.labelLarge)
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            val card = state.selectedCard
            SimpleListRow(
                title = if (card != null) "${card.type.title} • ${maskCardNumber(card.number)}" else "Kartani tanlang",
                subtitle = card?.let { formatSum(it.balance) },
                trailingText = "O'zgartirish",
                // To'liq ekranli tanlash → tanlov umumiy ViewModel'ga yoziladi (natija klassisiz)
                onClick = dropUnlessResumed { onIntent(Intent.SelectCardClicked) },
            )
        }

        OutlinedTextField(
            value = state.recipient,
            onValueChange = { value -> onIntent(Intent.RecipientChanged(value)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Qabul qiluvchi karta yoki telefon") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        OutlinedTextField(
            value = state.amount,
            onValueChange = { value -> onIntent(Intent.AmountChanged(value)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Summa") },
            suffix = { Text("so'm") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        if (errorText != null) {
            Text(text = errorText, color = MaterialTheme.colorScheme.error)
        }

        Button(
            // Dialog MANZILI ochiladi (TransferConfirmKey) — u ham shu ViewModel'ni ishlatadi
            onClick = dropUnlessResumed { onIntent(Intent.ContinueClicked) },
            enabled = state.canContinue,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Davom etish")
            }
        }
    }
}

@Preview
@Composable
private fun TransferFormScreenPreview() {
    MyNav3Theme {
        TransferFormScreen(
            state = UIState(
                isLoading = false,
                cards = PreviewData.cards,
                selectedCard = PreviewData.cards.first(),
                recipient = "8600111122223333",
                amount = "150000",
            ),
            onIntent = {},
        )
    }
}
