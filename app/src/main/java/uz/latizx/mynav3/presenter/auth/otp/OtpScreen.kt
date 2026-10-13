package uz.latizx.navigation3.presentation.auth.otp

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
import uz.latizx.mynav3.presenter.auth.otp.OtpViewModel
import uz.latizx.mynav3.presenter.auth.otp.OtpScreenContract.Effect
import uz.latizx.mynav3.presenter.auth.otp.OtpScreenContract.Intent
import uz.latizx.mynav3.presenter.auth.otp.OtpScreenContract.UIState
import uz.latizx.mynav3.ui.theme.MyNav3Theme
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.formatPhone

@Composable
fun OtpRoute(
    viewModel: OtpViewModel,
    onVerified: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            Effect.NavigateToMain -> onVerified()
            Effect.NavigateBack -> onNavigateBack()
        }
    }

    OtpScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun OtpScreen(
    state: UIState,
    onIntent: (Intent) -> Unit,
) {
    val errorText = state.error
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Tasdiqlash",
                onBack = dropUnlessResumed { onIntent(Intent.BackClicked) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(24.dp),
        ) {
            Text(
                text = "${formatPhone(state.phone)} raqamiga yuborilgan kodni kiriting",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = state.code,
                onValueChange = { value -> onIntent(Intent.CodeChanged(value)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("SMS kod") },
                singleLine = true,
                enabled = !state.isLoading,
                isError = errorText != null,
                supportingText = if (errorText != null) {
                    { Text(errorText) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onIntent(Intent.VerifyClicked) },
                enabled = state.canVerify,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Tasdiqlash")
                }
            }
            TextButton(
                onClick = { onIntent(Intent.ResendClicked) },
                enabled = state.canResend,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (state.resendSeconds > 0) "Kodni qayta yuborish (${state.resendSeconds} s)"
                    else "Kodni qayta yuborish",
                )
            }
        }
    }
}

@Preview
@Composable
private fun OtpScreenPreview() {
    MyNav3Theme {
        OtpScreen(state = UIState(phone = "901234567", code = "12"), onIntent = {})
    }
}
