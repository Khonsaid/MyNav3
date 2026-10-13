@file:OptIn(ExperimentalMaterial3Api::class)

package uz.latizx.mynav3.presenter.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import uz.latizx.mynav3.R

/**
 * Umumiy TopAppBar. [onBack] null bo'lsa "←" ko'rsatilmaydi (tab ildizlari — ulardan
 * "yuqoriga" yo'q, faqat tizim Back tugmasi uy tabga qaytaradi).
 */
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = "Orqaga",
                    )
                }
            }
        },
        actions = actions,
    )
}
