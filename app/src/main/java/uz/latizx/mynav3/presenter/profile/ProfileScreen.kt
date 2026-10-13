package uz.latizx.mynav3.presenter.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import uz.latizx.mynav3.core.mvi.CollectEffect
import uz.latizx.mynav3.domain.model.AppLanguage
import uz.latizx.mynav3.domain.model.UserProfile
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.LoadingView
import uz.latizx.mynav3.presenter.common.PreviewData
import uz.latizx.mynav3.presenter.common.SimpleListRow
import uz.latizx.mynav3.presenter.profile.ProfileScreenContract.Effect
import uz.latizx.mynav3.presenter.profile.ProfileScreenContract.Intent
import uz.latizx.mynav3.presenter.profile.ProfileScreenContract.UIState
import uz.latizx.mynav3.ui.theme.MyNav3Theme

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel,
    onOpenLanguageSheet: (current: AppLanguage) -> Unit,
    onOpenLogoutDialog: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is Effect.OpenLanguageSheet -> onOpenLanguageSheet(effect.current)
            Effect.OpenLogoutDialog -> onOpenLogoutDialog()
            Effect.LoggedOut -> onLoggedOut()
        }
    }

    ProfileScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun ProfileScreen(
    state: UIState,
    onIntent: (Intent) -> Unit,
) {
    Scaffold(topBar = { AppTopBar(title = "Profil") }) { padding ->
        val profile = state.profile
        if (state.isLoading || profile == null) {
            LoadingView(Modifier.padding(padding))
        } else {
            ProfileContent(
                profile = profile,
                isLoggingOut = state.isLoggingOut,
                onIntent = onIntent,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfile,
    isLoggingOut: Boolean,
    onIntent: (Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = profile.fullName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = profile.phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        // Bottom sheet MANZILI + natija (aniq kalit bilan)
        SimpleListRow(
            title = "Til",
            subtitle = profile.language.displayName,
            onClick = dropUnlessResumed { onIntent(Intent.LanguageClicked) },
        )
        HorizontalDivider()
        // Dialog MANZILI + natija → logout (barcha stack'lar tozalanadi)
        SimpleListRow(
            title = if (isLoggingOut) "Chiqilmoqda..." else "Chiqish",
            titleColor = MaterialTheme.colorScheme.error,
            enabled = !isLoggingOut,
            onClick = dropUnlessResumed { onIntent(Intent.LogoutClicked) },
        )
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    MyNav3Theme {
        ProfileScreen(state = UIState(isLoading = false, profile = PreviewData.profile), onIntent = {})
    }
}
