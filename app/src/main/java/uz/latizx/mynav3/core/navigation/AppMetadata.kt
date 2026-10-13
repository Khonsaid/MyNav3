package uz.latizx.mynav3.core.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.DialogSceneStrategy

/**
 * Entry METADATA — Navigation 3'da ekran haqidagi qo'shimcha ma'lumot (`Map<String, Any>`).
 * NavDisplay va SceneStrategy'lar uni o'qiydi: dialogmi, bottom sheetmi, qanday animatsiya...
 * Biz ham o'z kalitimizni qo'shamiz: pastki panel ko'rinadimi.
 *
 * Tip-xavfsiz kalit: `object X : NavMetadataKey<T>` + `metadata { put(X, value) }` +
 * `entry.metadata[X]`.
 *
 * Pastki panel (bottom bar) FAQAT shu kalitli ekranlarda (tab ildizlarida) ko'rinadi —
 * detal ekranlar butun ekranni egallaydi (qarang: Nav3BankApp, `showBottomBar`).
 */
object ShowBottomBarKey : NavMetadataKey<Boolean>

object AppMetadata {

    /** Tab ildizi (Kartalar, O'tkazmalar, Profil): pastki panel ko'rinadi + tablar orasida fade. */
    fun topLevel(): Map<String, Any> =
        metadata { put(ShowBottomBarKey, true) } + NavTransitions.fade()
}

/** Dialog yoki bottom sheet — ya'ni oldingi ekran ustida "suzib" turadigan entry. */
fun NavEntry<*>.isOverlay(): Boolean =
    DialogSceneStrategy.Companion.DialogKey in metadata || BottomSheetKey in metadata

/**
 * Eng yuqoridagi ekran tab ildizimi (pastki panel ko'rinishi kerakmi)? OVERLAY BO'LMAGAN entry'ga
 * qaraymiz: Profil ustida "Chiqish" dialogi ochilsa ham pastki panel yo'qolmasligi kerak.
 */
fun List<NavEntry<NavKey>>.shouldShowBottomBar(): Boolean =
    lastOrNull { entry -> !entry.isOverlay() }?.metadata?.get(ShowBottomBarKey) == true
