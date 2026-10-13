package uz.latizx.mynav3.presenter.transfer.confirm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uz.latizx.mynav3.presenter.common.DialogCard
import uz.latizx.mynav3.presenter.common.formatSum
import uz.latizx.mynav3.ui.theme.MyNav3Theme

/**
 * DIALOG MANZILI: `entry<TransferConfirmKey>(metadata = DialogSceneStrategy.dialog() + parent(form))`.
 * Bu Composable holatsiz (faqat qiymatlarni chizadi). Qiymatlarni entry o'tkazma oqimining
 * UMUMIY ViewModel'idan oladi, "Tasdiqlash" esa o'sha ViewModel'ga Intent bo'lib boradi
 * (qarang: TransferNavigation.kt).
 */
@Composable
fun TransferConfirmDialog(
    fromCardMasked: String,
    recipient: String,
    amount: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    DialogCard(
        title = "O'tkazmani tasdiqlang",
        confirmText = "Tasdiqlash",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ConfirmLine(label = "Kartadan", value = fromCardMasked)
            ConfirmLine(label = "Qabul qiluvchi", value = recipient)
            ConfirmLine(label = "Summa", value = formatSum(amount))
        }
    }
}

@Composable
private fun ConfirmLine(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Preview
@Composable
private fun TransferConfirmDialogPreview() {
    MyNav3Theme {
        TransferConfirmDialog(
            fromCardMasked = "8600 •••• •••• 1234",
            recipient = "8600111122223333",
            amount = 150_000,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
