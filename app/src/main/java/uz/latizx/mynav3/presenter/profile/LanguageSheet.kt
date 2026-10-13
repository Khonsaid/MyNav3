package uz.latizx.mynav3.presenter.profile

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
import uz.latizx.mynav3.domain.model.AppLanguage
import uz.latizx.mynav3.presenter.common.SimpleListRow
import uz.latizx.mynav3.ui.theme.MyNav3Theme

/**
 * BOTTOM SHEET MANZILI (holatsiz). Tanlangan til natija sifatida qaytadi:
 * `resultBus.sendResult(LanguageSheetKey.RESULT_KEY, language)` → `navigator.goBack()`.
 */
@Composable
fun LanguageSheet(
    current: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Ilova tili",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        AppLanguage.entries.forEach { language ->
            SimpleListRow(
                title = language.displayName,
                leading = { RadioButton(selected = language == current, onClick = null) },
                onClick = { onSelect(language) },
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Preview
@Composable
private fun LanguageSheetPreview() {
    MyNav3Theme {
        LanguageSheet(current = AppLanguage.UZ, onSelect = {})
    }
}
