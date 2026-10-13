package uz.latizx.mynav3.presenter.auth.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.latizx.mynav3.core.mvi.CollectEffect
import uz.latizx.mynav3.presenter.auth.splash.SplashScreenContract.Effect
import uz.latizx.mynav3.presenter.auth.splash.SplashScreenContract.Intent
import uz.latizx.mynav3.presenter.auth.splash.SplashScreenContract.UIState
import uz.latizx.mynav3.ui.theme.MyNav3Theme

/**
 * ROUTE — "aqlli" qism: ViewModel'ni ulaydi, Effect'larni navigatsiya callback'lariga aylantiradi.
 * Route NavKey'larni ham, Navigator'ni ham bilmaydi — faqat lambda'lar (`onNavigateToMain`).
 */
@Composable
fun SplashRoute(
    viewModel: SplashViewModel,
    onNavigateToMain: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            Effect.NavigateToMain -> onNavigateToMain()
            Effect.NavigateToLogin -> onNavigateToLogin()
        }
    }

    SplashScreen(state = state, onIntent = viewModel::onIntent)
}

/** SCREEN — "ahmoq" qism: faqat UIState'ni chizadi va Intent yuboradi (Preview'da ishlaydi). */
@Composable
fun SplashScreen(
    state: UIState,
    onIntent: (Intent) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Nav3 Bank",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(24.dp))
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.error != null) {
                Text(text = state.error, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { onIntent(Intent.RetryClicked) }) { Text("Qayta urinish") }
            }
        }
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    MyNav3Theme {
        SplashScreen(state = UIState(), onIntent = {})
    }
}
