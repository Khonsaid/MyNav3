package uz.latizx.mynav3.presenter.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Dialog MANZILI (destination) uchun kontent.
 *
 * MUHIM: `DialogSceneStrategy` entry'ni o'zi `Dialog { }` oynasiga o'raydi. Shuning uchun
 * entry ichida yana `AlertDialog(...)` chaqirilmaydi (aks holda ikki qavat oyna/scrim bo'ladi) —
 * faqat dialogning ichki ko'rinishi chiziladi.
 */
@Composable
fun DialogCard(
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String = "Bekor qilish",
    destructive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.widthIn(min = 280.dp, max = 560.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            content()
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = onDismiss) { Text(dismissText) }
                TextButton(onClick = onConfirm) {
                    Text(
                        text = confirmText,
                        color = if (destructive) MaterialTheme.colorScheme.error else Color.Unspecified,
                    )
                }
            }
        }
    }
}
