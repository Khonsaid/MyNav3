package uz.latizx.mynav3.presenter.cards.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import uz.latizx.mynav3.presenter.common.DialogCard
import uz.latizx.mynav3.ui.theme.MyNav3Theme

/**
 * DIALOG MANZILI: `entry<BlockCardDialogKey>(metadata = DialogSceneStrategy.dialog())`.
 *
 * Biznes-mantiqsiz oddiy tasdiqlash oynasi — shuning uchun ViewModel yo'q. Natija
 * (`BlockCardResult`) navigatsiya qatlamida `ResultEventBus` orqali qaytariladi.
 */
@Composable
fun BlockCardDialog(
    block: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    DialogCard(
        title = if (block) "Kartani bloklash" else "Blokdan chiqarish",
        confirmText = if (block) "Bloklash" else "Chiqarish",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        destructive = block,
    ) {
        Text(
            text = if (block) {
                "Bloklangan karta bilan to'lov va o'tkazmalar amalga oshirilmaydi. Davom etasizmi?"
            } else {
                "Karta yana to'lov va o'tkazmalar uchun faollashtiriladi."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun BlockCardDialogPreview() {
    MyNav3Theme {
        BlockCardDialog(block = true, onConfirm = {}, onDismiss = {})
    }
}
