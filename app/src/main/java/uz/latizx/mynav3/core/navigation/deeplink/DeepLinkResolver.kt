package uz.latizx.mynav3.core.navigation.deeplink

import android.content.Intent
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.BackStackMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.invoke
import javax.inject.Inject

/**
 * Intent → sintetik back stack (`List<NavKey>`).
 *
 * 1. `DeepLinkRequest(intent)` — Intent'dagi URI, action, mime-type'dan so'rov yasaydi.
 * 2. Har bir matcher bilan solishtiradi (`match`), mos kelganlarni oladi.
 * 3. `maxOrNull()` — eng aniq mos kelganini tanlaydi (Navigation 3 MatchResult'lari
 *    `Comparable`: aniq yo'l > argumentli yo'l > ...).
 * 4. `backStack` — `withBackStack { }`da qurilgan ro'yxat.
 */
class DeepLinkResolver @Inject constructor(
    contributors: Set<@JvmSuppressWildcards DeepLinkContributor>,
) {

    private val matchers: List<BackStackMatcher<NavKey, NavKey>> =
        contributors.flatMap { contributor -> contributor.matchers() }

    /** Mos deep link bo'lmasa `null` (ilova odatdagidek ochiladi). */
    fun resolve(intent: Intent): List<NavKey>? {
        // URI'siz Intent (masalan launcher'dan ochish) — deep link emas
        if (intent.data == null) return null
        val request = DeepLinkRequest(intent)
        return matchers
            .mapNotNull { matcher -> matcher.match(request) }
            .maxOrNull()
            ?.backStack
    }
}
