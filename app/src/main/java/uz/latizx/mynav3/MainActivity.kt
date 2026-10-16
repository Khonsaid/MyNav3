package uz.latizx.mynav3

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import uz.latizx.mynav3.core.navigation.EntryProviderInstaller
import uz.latizx.mynav3.presenter.app.Nav3AppContent
import uz.latizx.mynav3.ui.theme.MyNav3Theme
import uz.latizx.mynav3.core.navigation.deeplink.DeepLinkResolver
import uz.latizx.mynav3.core.navigation.deeplink.DeepLinkTarget
import uz.latizx.mynav3.domain.model.AppTheme
import uz.latizx.mynav3.domain.repository.SettingsRepository
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
    private val deepLinkChannel = Channel<DeepLinkTarget>(Channel.BUFFERED)
    private val deepLinks: Flow<DeepLinkTarget> = deepLinkChannel.receiveAsFlow()

    @Inject
    lateinit var settingsRepository: SettingsRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // COLD START deep link: faqat Activity birinchi marta yaratilganda.
        // savedInstanceState != null bo'lsa (rotation yoki process death'dan tiklanish),
        // back stack allaqachon tiklanadi — deep link'ni qayta qo'llash xato bo'lardi. 600f31d5f2af9924
        if (savedInstanceState == null) {
            handleDeepLink(intent)
        }

        Log.d("TTT getCdid", "onCreate: ${getCdid(this)}")

        setContent {
            val theme by settingsRepository.observeTheme().collectAsStateWithLifecycle(AppTheme.SYSTEM)
            val darkTheme = when (theme) {
                AppTheme.SYSTEM -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            MyNav3Theme(darkTheme = darkTheme) {
                Nav3AppContent(
                    entryInstallers = entryInstallers,
                    deepLinks = deepLinks
                )
            }
        }
    }

    @SuppressLint("HardwareIds")
    fun getCdid(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    /** WARM deep link: ilova ochiq turganda (launchMode="singleTop") kelgan yangi Intent. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val backStack = deepLinkResolver.resolve(intent) ?: return
        // Intent o'zi aytsa — shuni olamiz, aytmasa — umumiy sozlama
        val exitOnBack = intent.getBooleanExtra(EXTRA_EXIT_ON_BACK, DEFAULT_EXIT_ON_BACK)
        deepLinkChannel.trySend(DeepLinkTarget(backStack, exitOnBack))
    }

    companion object {
        const val EXTRA_EXIT_ON_BACK = "exit_on_back"

        /** BUTUN ILOVA uchun kalit: true — Back = chiqish, false — ota ekranlar bo'ylab qaytish. */
        const val DEFAULT_EXIT_ON_BACK = true
    }
}