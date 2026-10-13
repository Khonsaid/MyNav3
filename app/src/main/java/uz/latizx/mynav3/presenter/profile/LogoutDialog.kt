package uz.latizx.mynav3.presenter.profile

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import uz.latizx.mynav3.presenter.common.DialogCard
import uz.latizx.mynav3.ui.theme.MyNav3Theme

/** DIALOG MANZILI: chiqishni tasdiqlash. Natija — `LogoutConfirmedResult`. */
@Composable
fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    DialogCard(
        title = "Hisobdan chiqish",
        confirmText = "Chiqish",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        destructive = true,
    ) {
        Text(
            text = "Barcha ochiq ekranlar yopiladi va qayta kirish uchun SMS kod kerak bo'ladi.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun LogoutDialogPreview() {
    MyNav3Theme {
        LogoutDialog(onConfirm = {}, onDismiss = {})
    }
}
