package uz.latizx.mynav3.core.navigation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope

/**
 * SHARED ELEMENT ("uchib o'tadigan" umumiy element) — Navigation 3 bilan.
 *
 * Qanday ishlaydi:
 *  1. `Nav3BankApp`: `SharedTransitionLayout { NavDisplay(...) }` — scope shu
 *     [LocalSharedTransitionScope] orqali pastga uzatiladi.
 *  2. NavDisplay sahnalar (scene) orasidagi o'tishni `AnimatedContent` bilan bajaradi va har bir
 *     entry ichida [LocalNavAnimatedContentScope] ni beradi (qaysi ekran kirib, qaysi chiqyapti).
 *  3. Ikki ekrandagi bir xil `key`li element (`Modifier.navSharedElement(key)`) o'tish paytida
 *     birinchi ekrandagi joyidan ikkinchisidagi joyiga uchib o'tadi; Back'da — teskari.
 *
 * Qachon ishlamaydi:
 *  - Preview'da — scope yo'q (null), modifier hech narsa qilmaydi (bu to'g'ri);
 *  - element ikkinchi ekranda o'tish tugagandan keyin paydo bo'lsa (masalan, ma'lumot hali
 *    yuklanayotgan bo'lsa). Bizda karta xotiradagi StateFlow'dan sinxron keladi, shuning uchun
 *    detal ekrani kartani birinchi kadrdanoq ko'rsatadi. Tarmoqdan yuklansa — ro'yxatdagi
 *    ma'lumotni key orqali uzating yoki keshdan oling.
 */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * Elementni ekranlar orasida "uchadigan" qiladi. [key] ikkala ekranda bir xil bo'lishi kerak,
 * masalan `"bank-card-${card.id}"`. Faqat NavEntry ichida ishlatiladi.
 */
@Composable
fun Modifier.navSharedElement(key: Any): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return this
    // NavDisplay'ning AnimatedContent scope'i: element qachon ko'rinib/yashirinayotganini biladi
    val animatedContentScope = LocalNavAnimatedContentScope.current
    return with(sharedTransitionScope) {
        this@navSharedElement.sharedElement(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedContentScope,
        )
    }
}
