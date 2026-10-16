package uz.latizx.mynav3.presenter.settings.security.devices

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import uz.latizx.mynav3.presenter.common.DialogCard

/** DIALOG MANZILI (holatsiz). Natija — `TerminateDeviceConfirmed`. */
@Composable
fun TerminateDeviceDialog(
    deviceName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    DialogCard(
        title = "Qurilmani chiqarish",
        confirmText = "Chiqarish",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        destructive = true,
    ) {
        Text(
            text = "\"$deviceName\" hisobingizdan chiqariladi. Qayta kirish uchun SMS kod kerak bo'ladi.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}