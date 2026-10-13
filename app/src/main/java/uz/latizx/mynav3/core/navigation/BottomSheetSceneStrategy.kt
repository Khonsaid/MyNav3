@file:OptIn(ExperimentalMaterial3Api::class)

package uz.latizx.mynav3.core.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/**
 * Bottom sheet — alohida NAVIGATSIYA MANZILI sifatida (rasmiy nav3-recipes "bottomsheet" asosida).
 *
 * Nega oddiy `if (show) ModalBottomSheet {}` emas?
 *  - sheet back stack'da turadi → Back tugmasi/predictive back uni to'g'ri yopadi;
 *  - rotation va process death'dan keyin ham ochiq holda tiklanadi;
 *  - sheet'dan natija qaytarish ([androidx.navigation3.runtime.result.ResultEventBus]) va
 *    sheet'dan boshqa ekranga o'tish oddiy navigatsiya kabi ishlaydi.
 *
 * Ishlatish:
 * ```
 * entry<LanguageSheetKey>(metadata = BottomSheetSceneStrategy.bottomSheet()) { ... }
 * ```
 * va `NavDisplay(sceneStrategies = listOf(..., bottomSheetStrategy, ...))`.
 */
class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull() ?: return null
        val config = lastEntry.metadata[BottomSheetKey] ?: return null
        return BottomSheetScene(
            key = lastEntry.contentKey,
            previousEntries = entries.dropLast(1),
            // OverlayScene: sheet ostida oldingi ekranlar ko'rinib turadi
            overlaidEntries = entries.dropLast(1),
            entry = lastEntry,
            config = config,
            onBack = onBack,
        )
    }

    companion object {
        /** Entry metadata: shu entry bottom sheet bo'lib ko'rsatiladi. */
        fun bottomSheet(config: BottomSheetConfig = BottomSheetConfig()): Map<String, Any> =
            metadata { put(BottomSheetKey, config) }
    }
}

/** Bottom sheet sozlamalari (eksperimental Material API'ni tashqariga chiqarmaslik uchun). */
data class BottomSheetConfig(
    val skipPartiallyExpanded: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
)

/** Metadata kaliti: entry'da shu kalit bo'lsa — u bottom sheet. */
object BottomSheetKey : NavMetadataKey<BottomSheetConfig>

private data class BottomSheetScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val config: BottomSheetConfig,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    /** [onRemove]da yopilish animatsiyasini o'ynash uchun saqlab qo'yamiz. */
    private var sheetState: SheetState? = null

    override val content: @Composable () -> Unit = {
        // material3 1.5+ da `rememberBottomSheetState` tavsiya etiladi; bu variant 1.4 va 1.5 da ishlaydi.
        @Suppress("DEPRECATION")
        val state = rememberModalBottomSheetState(skipPartiallyExpanded = config.skipPartiallyExpanded)
        sheetState = state
        ModalBottomSheet(
            // Pastga surish / tashqariga bosish / Back → navigator.goBack() → entry stack'dan chiqadi
            onDismissRequest = onBack,
            sheetState = state,
            properties = ModalBottomSheetProperties(
                securePolicy = SecureFlagPolicy.Inherit,
                shouldDismissOnBackPress = true,
                shouldDismissOnClickOutside = config.dismissOnClickOutside,
            ),
        ) {
            entry.Content()
        }
    }

    /**
     * Entry back stack'dan olib tashlanganda NavDisplay shu funksiyani kutadi va keyin sahnani
     * olib tashlaydi → sheet "sakrab" yo'qolmaydi, silliq pastga tushadi.
     */
    override suspend fun onRemove() {
        sheetState?.hide()
    }
}
