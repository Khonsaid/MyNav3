package uz.latizx.mynav3.core.navigation.deeplink

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.BackStackMatcher

/**
 * Har bir feature o'zi qo'llab-quvvatlaydigan deep link'larni shu interfeys orqali beradi
 * (Hilt `@IntoSet`). [DeepLinkResolver] hammasini yig'ib, Intent'ni moslaydi.
 *
 * Navigation 3 deep link API (navigation3-runtime 1.2.0):
 * ```
 * UriDeepLinkMatcher(Uri.parse("nav3bank://cards/{cardId}"), CardDetailKey.serializer())
 *     .withBackStack { match -> listOf(CardsKey, match.key) }   // sintetik back stack
 * ```
 *  - `{cardId}` → `CardDetailKey.cardId` (nomlar bir xil bo'lishi shart, serializer orqali);
 *  - `withBackStack` → Back bosilganda ilovadan chiqib ketmasdan, mantiqiy ota ekranga
 *    (Kartalar ro'yxati) qaytish uchun.
 */
fun interface DeepLinkContributor {
    fun matchers(): List<BackStackMatcher<NavKey, NavKey>>
}
