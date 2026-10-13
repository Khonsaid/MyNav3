package uz.latizx.mynav3.presenter.cards.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uz.latizx.mynav3.presenter.common.SimpleListRow
import uz.latizx.mynav3.ui.theme.MyNav3Theme

/**
 * BOTTOM SHEET MANZILI: `entry<CardActionsSheetKey>(metadata = BottomSheetSceneStrategy.bottomSheet())`.
 *
 * Kontent `ModalBottomSheet` ichida chiziladi (BottomSheetScene'da). Sheet'dan boshqa ekranga
 * o'tish — oddiy navigatsiya: `navigator.replaceCurrent(CardATMKey(...))`.
 */
@Composable
fun CardActionsSheet(
    isBlocked: Boolean,
    onAtmsClick: () -> Unit,
    onTransferClick: () -> Unit,
    onBlockToggleClick: () -> Unit,
    onClose: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Karta amallari",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        SimpleListRow(
            title = "Bankomatlar",
            subtitle = "Sheet o'rniga yangi ekran (replaceCurrent)",
            onClick = onAtmsClick,
        )
        SimpleListRow(
            title = "O'tkazma qilish",
            subtitle = "Sheet yopiladi + O'tkazmalar tabi (cross-tab)",
            enabled = !isBlocked,
            onClick = onTransferClick,
        )
        SimpleListRow(
            title = if (isBlocked) "Blokdan chiqarish" else "Bloklash",
            subtitle = "Sheet o'rniga dialog (replaceCurrent)",
            titleColor = if (isBlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            onClick = onBlockToggleClick,
        )
        SimpleListRow(title = "Yopish", onClick = onClose)
        Spacer(Modifier.height(16.dp))
    }
}

@Preview
@Composable
private fun CardActionsSheetPreview() {
    MyNav3Theme {
        CardActionsSheet(isBlocked = false, onAtmsClick = {}, onTransferClick = {}, onBlockToggleClick = {}, onClose = {})
    }
}
