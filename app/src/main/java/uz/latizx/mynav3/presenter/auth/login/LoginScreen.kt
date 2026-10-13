package uz.latizx.mynav3.presenter.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import uz.latizx.mynav3.core.mvi.CollectEffect
import uz.latizx.mynav3.presenter.auth.login.LoginScreenContract.Effect
import uz.latizx.mynav3.presenter.auth.login.LoginScreenContract.Intent
import uz.latizx.mynav3.presenter.auth.login.LoginScreenContract.UIState
import uz.latizx.mynav3.ui.theme.MyNav3Theme

@Composable
fun LoginRoute(
    viewModel: LoginViewModel,
    onNavigateToOtp: (phone: String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is Effect.NavigateToOtp -> onNavigateToOtp(effect.phone)
        }
    }

    LoginScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun LoginScreen(
    state: UIState,
    onIntent: (Intent) -> Unit,
) {
    val errorText = state.error
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Xush kelibsiz!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Telefon raqamingizni kiriting — tasdiqlash kodi yuboramiz.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = state.phone,
                onValueChange = { value -> onIntent(Intent.PhoneChanged(value)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Telefon raqam") },
                prefix = { Text("+998 ") },
                placeholder = { Text("901234567") },
                singleLine = true,
                isError = errorText != null,
                supportingText = if (errorText != null) {
                    { Text(errorText) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                // dropUnlessResumed: ekran o'tish animatsiyasi paytida (entry RESUMED emas)
                // bosishlarni e'tiborsiz qoldiradi → ikki marta navigatsiya bo'lmaydi.
                onClick = dropUnlessResumed { onIntent(Intent.ContinueClicked) },
                enabled = state.isContinueEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Davom etish")
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Demo: istalgan 9 xonali raqam, SMS kod — 1234",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    MyNav3Theme {
        LoginScreen(state = UIState(phone = "901234567"), onIntent = {})
    }
}
