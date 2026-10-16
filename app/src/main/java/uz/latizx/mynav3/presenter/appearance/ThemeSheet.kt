package uz.latizx.mynav3.presenter.appearance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uz.latizx.mynav3.domain.model.AppTheme
import uz.latizx.mynav3.presenter.common.SimpleListRow
import uz.latizx.mynav3.ui.theme.MyNav3Theme

/** BOTTOM SHEET MANZILI (holatsiz) — LanguageSheet bilan bir xil naqsh. */
@Composable
fun ThemeSheet(
    current: AppTheme,
    onSelect: (AppTheme) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Mavzu",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        AppTheme.entries.forEach { theme ->
            SimpleListRow(
                title = theme.displayName,
                leading = { RadioButton(selected = theme == current, onClick = null) },
                onClick = { onSelect(theme) },
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Preview
@Composable
private fun ThemeSheetPreview() {
    MyNav3Theme { ThemeSheet(current = AppTheme.DARK, onSelect = {}) }
}