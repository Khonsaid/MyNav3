package uz.latizx.mynav3.presenter.transfer.success

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import uz.latizx.mynav3.R
import uz.latizx.mynav3.presenter.common.formatSum
import uz.latizx.mynav3.ui.theme.MyNav3Theme

/**
 * Muvaffaqiyat ekrani — forma O'RNIGA ochiladi (`replaceCurrent`), shuning uchun:
 *  - tizim Back → o'tkazmalar tarixi (forma qayta ko'rinmaydi, ikki marta o'tkazma xavfi yo'q);
 *  - "Tayyor" → `popUpTo(TransfersKey)`;
 *  - "Yana o'tkazma" → `replaceCurrent(TransferFormKey())`.
 *
 * Holatsiz ekran: barcha ma'lumot key'da, ViewModel kerak emas.
 */
@Composable
fun TransferSuccessScreen(
    amount: Long,
    transferId: String,
    onDone: () -> Unit,
    onNewTransfer: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_circle),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(96.dp),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = formatSum(amount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(text = "muvaffaqiyatli o'tkazildi", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "ID: $transferId",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(32.dp))
            Button(onClick = dropUnlessResumed(block = onDone), modifier = Modifier.fillMaxWidth()) {
                Text("Tayyor")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = dropUnlessResumed(block = onNewTransfer), modifier = Modifier.fillMaxWidth()) {
                Text("Yana o'tkazma")
            }
        }
    }
}

@Preview
@Composable
private fun TransferSuccessScreenPreview() {
    MyNav3Theme {
        TransferSuccessScreen(amount = 150_000, transferId = "tr-1", onDone = {}, onNewTransfer = {})
    }
}
