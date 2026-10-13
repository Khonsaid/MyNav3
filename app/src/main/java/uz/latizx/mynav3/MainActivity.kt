package uz.latizx.mynav3

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import uz.latizx.mynav3.core.navigation.EntryProviderInstaller
import uz.latizx.mynav3.presenter.app.Nav3AppContent
import uz.latizx.mynav3.ui.theme.MyNav3Theme
import uz.latizx.mynav3.core.navigation.deeplink.DeepLinkResolver
import javax.inject.Inject

/**
 * Yagona Activity (single-activity arxitektura).
 *
 * Bu yerda faqat 3 ish qilinadi:
 *  1. Hilt'dan barcha feature'larning entry installer'lari (multibinding Set) olinadi.
 *  2. Deep link Intent'lari NavKey ro'yxatiga (sintetik back stack) aylantiriladi.
 *  3. Compose UI ([Nav3AppContent]) ishga tushiriladi.
 *
 * Navigatsiya holati Activity'da emas — Compose ichida `rememberNavBackStack` orqali saqlanadi,
 * shuning uchun konfiguratsiya o'zgarishi (rotation) va process death'dan keyin ham tiklanadi.
 */

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Har bir feature (auth, cards, transfer, profile) o'z ekranlarini `@IntoSet` bilan beradi.
     * `@JvmSuppressWildcards` — Kotlin `Set<? extends ...>` generatsiya qilmasligi uchun
     * (aks holda Hilt multibinding'ni topa olmaydi).
     */
    @Inject
    lateinit var entryInstallers: Set<@JvmSuppressWildcards EntryProviderInstaller>

    @Inject
    lateinit var deepLinkResolver: DeepLinkResolver

    /**
     * Activity → Compose: deep link'lar kanali. Channel bufer sifatida ishlaydi: Compose hali
     * kolleksiya qilishni boshlamagan bo'lsa ham (cold start) deep link yo'qolmaydi.
     */
    private val deepLinkChannel = Channel<List<NavKey>>(Channel.BUFFERED)
    private val deepLinks: Flow<List<NavKey>> = deepLinkChannel.receiveAsFlow()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // COLD START deep link: faqat Activity birinchi marta yaratilganda.
        // savedInstanceState != null bo'lsa (rotation yoki process death'dan tiklanish),
        // back stack allaqachon tiklanadi — deep link'ni qayta qo'llash xato bo'lardi.
        if (savedInstanceState == null) {
            handleDeepLink(intent)
        }


        setContent {
            MyNav3Theme {
                Nav3AppContent(
                    entryInstallers = entryInstallers,
                    deepLinks = deepLinks
                )
            }
        }
    }


    /** WARM deep link: ilova ochiq turganda (launchMode="singleTop") kelgan yangi Intent. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val backStack = deepLinkResolver.resolve(intent) ?: return
        deepLinkChannel.trySend(backStack)
    }
}