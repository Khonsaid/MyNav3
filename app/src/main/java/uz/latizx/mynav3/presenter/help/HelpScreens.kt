package uz.latizx.mynav3.presenter.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import uz.latizx.mynav3.presenter.common.AppTopBar
import uz.latizx.mynav3.presenter.common.EmptyView
import uz.latizx.mynav3.presenter.common.SimpleListRow
import uz.latizx.mynav3.ui.theme.MyNav3Theme

/*
 * YORDAM bo'limi — o'zgarmas (statik) matnlar. Holat ham, server ham yo'q, shuning uchun
 * Contract/ViewModel yozilmaydi: oddiy Composable + navigatsiya lambda'lari yetarli.
 * MVI har doim shart emas — holat bo'lsagina kerak.
 */

data class Faq(val id: String, val question: String, val answer: String)

object FaqData {
    val items = listOf(
        Faq("limits", "Kunlik o'tkazma limiti qancha?", "Kartadan kartaga kuniga 50 mln so'mgacha o'tkazish mumkin."),
        Faq("block", "Kartani qanday bloklayman?", "Kartalar → kartani tanlang → \"Bloklash\". Blokdan chiqarish ham shu yerda."),
        Faq("pin", "PIN-kodni unutdim", "Hisobdan chiqing va SMS kod orqali qayta kiring, so'ng yangi PIN o'rnating."),
    )

    fun find(id: String): Faq? = items.firstOrNull { it.id == id }
}

@Composable
fun HelpScreen(
    onOpenFaq: (faqId: String) -> Unit,
    onOpenAbout: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(topBar = { AppTopBar(title = "Yordam", onBack = onNavigateBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "Ko'p beriladigan savollar",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(16.dp),
            )
            FaqData.items.forEach { faq ->
                SimpleListRow(title = faq.question, onClick = dropUnlessResumed { onOpenFaq(faq.id) })
                HorizontalDivider()
            }
            SimpleListRow(title = "Ilova haqida", onClick = dropUnlessResumed { onOpenAbout() })
        }
    }
}

@Composable
fun FaqDetailScreen(
    faqId: String,
    onNavigateBack: () -> Unit,
) {
    val faq = FaqData.find(faqId)
    Scaffold(topBar = { AppTopBar(title = "Savol-javob", onBack = onNavigateBack) }) { padding ->
        if (faq == null) {
            // Deep link'da noto'g'ri id kelishi mumkin
            EmptyView("Savol topilmadi", Modifier.padding(padding))
            return@Scaffold
        }
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(text = faq.question, style = MaterialTheme.typography.titleLarge)
            Text(text = faq.answer, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    Scaffold(topBar = { AppTopBar(title = "Ilova haqida", onBack = onNavigateBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = "MyNav3 Bank", style = MaterialTheme.typography.headlineSmall)
            Text(text = "Versiya 1.0", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Navigation 3 · Hilt · MVI", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview
@Composable
private fun HelpScreenPreview() {
    MyNav3Theme { HelpScreen(onOpenFaq = {}, onOpenAbout = {}, onNavigateBack = {}) }
}