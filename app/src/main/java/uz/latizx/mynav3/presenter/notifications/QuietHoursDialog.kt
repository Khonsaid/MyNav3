package uz.latizx.mynav3.presenter.notifications

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uz.latizx.mynav3.presenter.common.DialogCard
import uz.latizx.mynav3.ui.theme.MyNav3Theme


/**
 * DIALOG MANZILI. Kichik vaqtinchalik holat (tanlanayotgan soatlar) — ViewModel shart emas,
 * `rememberSaveable` yetarli (aylantirishda ham saqlanadi). Natija — `QuietHoursResult`.
 */
@Composable
fun QuietHoursDialog(
    initialFrom: Int,
    initialTo: Int,
    onConfirm: (fromHour: Int, toHour: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var from by rememberSaveable { mutableIntStateOf(initialFrom) }
    var to by rememberSaveable { mutableIntStateOf(initialTo) }

    DialogCard(
        title = "Jim soatlar",
        confirmText = "Saqlash",
        onConfirm = { onConfirm(from, to) },
        onDismiss = onDismiss,
    ) {
        HourPicker(label = "Boshlanishi", hour = from, onChange = { from = it })
        HourPicker(label = "Tugashi", hour = to, onChange = { to = it })
    }
}

@Composable
private fun HourPicker(label: String, hour: Int, onChange: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, modifier = Modifier.weight(1f))
        TextButton(onClick = { onChange((hour + 23) % 24) }) { Text("−") }
        Text(text = "%02d:00".format(hour), style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = { onChange((hour + 1) % 24) }) { Text("+") }
    }
}

@Preview
@Composable
private fun QuietHoursDialogPreview() {
    MyNav3Theme { QuietHoursDialog(initialFrom = 23, initialTo = 7, onConfirm = { _, _ -> }, onDismiss = {}) }
}