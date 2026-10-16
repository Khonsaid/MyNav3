package uz.latizx.mynav3.presenter.settings.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import uz.latizx.mynav3.core.mvi.CollectEffect
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.SimpleListRow
import uz.latizx.mynav3.ui.theme.MyNav3Theme

@Composable
fun SettingsHomeRoute(
    viewModel: SettingsHomeViewModel,
    onOpenSection: (SettingsSection) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is SettingsHomeScreenContract.Effect.OpenSection -> onOpenSection(effect.section)
        }
    }

    SettingsHomeScreen(state = state, onIntent = viewModel::onIntent)
}


@Composable
fun SettingsHomeScreen(
    state: SettingsHomeScreenContract.UIState,
    onIntent: (SettingsHomeScreenContract.Intent) -> Unit,
) {
    // Tab ildizi: "←" yo'q (onBack = null)
    Scaffold(topBar = { AppTopBar(title = "Sozlamalar") }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionRow("Xavfsizlik", "PIN, qurilmalar", SettingsSection.SECURITY, onIntent)
            HorizontalDivider()
            SectionRow("Bildirishnomalar", "Push, SMS, jim soatlar", SettingsSection.NOTIFICATIONS, onIntent)
            HorizontalDivider()
            SectionRow("Ko'rinish", "Mavzu: ${state.theme.displayName}", SettingsSection.APPEARANCE, onIntent)
            HorizontalDivider()
            SectionRow("Yordam", "Savol-javob, ilova haqida", SettingsSection.HELP, onIntent)
            HorizontalDivider()
            SimpleListRow(
                title = "Akkauntni o'chirish",
                titleColor = MaterialTheme.colorScheme.error,
                onClick = dropUnlessResumed { onIntent(SettingsHomeScreenContract.Intent.SectionClicked(SettingsSection.DELETE_ACCOUNT)) },
            )
        }
    }
}

@Composable
private fun SectionRow(
    title: String,
    subtitle: String,
    section: SettingsSection,
    onIntent: (SettingsHomeScreenContract.Intent) -> Unit,
) {
    SimpleListRow(
        title = title,
        subtitle = subtitle,
        // dropUnlessResumed: tez ikki marta bosilsa ekran ikki marta ochilmaydi
        onClick = dropUnlessResumed { onIntent(SettingsHomeScreenContract.Intent.SectionClicked(section)) },
    )
}

@Preview
@Composable
private fun SettingsHomeScreenPreview() {
    MyNav3Theme { SettingsHomeScreen(state = SettingsHomeScreenContract.UIState(), onIntent = {}) }
}