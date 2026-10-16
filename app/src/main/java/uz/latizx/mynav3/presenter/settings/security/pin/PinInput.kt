package uz.latizx.mynav3.presenter.settings.security.pin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

const val PIN_LENGTH = 4

/**
 * PIN kiritish maydoni — PinCheck va PIN almashtirishning 3 qadamida qayta ishlatiladi.
 * Faqat raqam, ko'pi bilan [PIN_LENGTH] ta. 4-raqam kiritilganda ViewModel o'zi tekshiradi.
 */
@Composable
fun PinInput(
    title: String,
    pin: String,
    error: String?,
    isLoading: Boolean,
    onPinChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { value ->
                if (value.length <= PIN_LENGTH && value.all(Char::isDigit)) onPinChange(value)
            },
            enabled = !isLoading,
            singleLine = true,
            isError = error != null,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.width(160.dp),
        )
        Spacer(Modifier.height(12.dp))
        when {
            isLoading -> CircularProgressIndicator()
            error != null -> Text(text = error, color = MaterialTheme.colorScheme.error)
            hint != null -> Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}