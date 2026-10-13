package uz.latizx.mynav3.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.savedstate.compose.LocalSavedStateRegistryOwner

/**
 * UMUMIY (shared) VIEWMODEL — bir nechta ekran (entry) uchun BITTA ViewModel nusxasi.
 *
 * Navigation 2'da buning uchun nested graph bor edi: `hiltNavGraphViewModels(R.id.transferGraph)`.
 * Navigation 3'da graph yo'q, shuning uchun ViewModel'ning "egasi" — oqimning birinchi ekrani
 * (PARENT), keyingi ekranlar (CHILD) metadata orqali parent'ni ko'rsatadi:
 *
 * ```
 * // PARENT: contentKey'ni o'zimiz belgilaymiz — child aynan shu kalit orqali topadi
 * entry<TransferFormKey>(clazzContentKey = { key -> key.toContentKey() }) {
 *     val viewModel = hiltViewModel<TransferFormViewModel, ...>(...)        // oddiy, o'z store'idan
 * }
 *
 * // CHILD: metadata'da parent'ni ko'rsatadi
 * entry<SelectCardKey>(metadata = { key -> SharedViewModelStoreNavEntryDecorator.parent(key.form) }) {
 *     val flowViewModel = hiltViewModel<TransferFormViewModel, ...>(
 *         viewModelStoreOwner = LocalSharedViewModelStoreOwner.current,       // parent'ning store'i
 *         creationCallback = { factory -> factory.create(key.form) },
 *     )
 *     val ownViewModel = hiltViewModel<SelectCardViewModel, ...>(...)       // o'zining store'i
 * }
 * ```
 *
 * Bu decorator kutubxonadagi `ViewModelStoreNavEntryDecorator`ni TO'LIQ almashtiradi:
 *  - har bir entry o'z ViewModelStore'ini oladi ([LocalViewModelStoreOwner]) — oddiy
 *    `hiltViewModel()` avvalgidek ishlaydi;
 *  - [parent] metadata'si bor entry QO'SHIMCHA ravishda parent'ning store'ini ham oladi
 *    ([LocalSharedViewModelStoreOwner]).
 *
 * Hayot sikli:
 *  - entry back stack'dan olib tashlanganda (onPop) faqat O'ZINING store'i tozalanadi;
 *  - parent'ning ViewModel'i child'lar ochilib-yopilsa ham yashaydi va parent pop bo'lganda
 *    (barcha child'lar kompozitsiyadan chiqqach) tozalanadi;
 *  - process death: child birinchi chizilsa ham ishlaydi — u parent'ning store'ini o'zi
 *    yaratadi, ViewModel esa child key'idagi parent key'i bilan yaratiladi.
 *
 * SHART: parent va child BIR back stack'da bo'lishi kerak — har bir stack'ning o'z
 * ViewModelStoreProvider'i bor (qarang: `NavigationState.toEntries`).
 *
 * Manba: github.com/android/nav3-recipes → `sharedviewmodel`.
 */
class SharedViewModelStoreNavEntryDecorator<T : Any>(
    viewModelStoreProvider: ViewModelStoreProvider,
) : NavEntryDecorator<T>(
    // Entry back stack'dan chiqdi va kompozitsiyani tark etdi → uning store'i tozalanadi
    onPop = { contentKey -> viewModelStoreProvider.clearKey(contentKey) },
    decorate = { entry ->
        // SaveableStateHolder decorator'i har bir entry'ga o'z SavedStateRegistryOwner'ini beradi
        // (SavedStateHandle uchun kerak), shuning uchun u ro'yxatda BU decorator'dan oldin turadi.
        val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current

        val ownOwner = rememberViewModelStoreOwner(
            key = entry.contentKey,
            provider = viewModelStoreProvider,
            savedStateRegistryOwner = savedStateRegistryOwner,
        )
        val providedValues = mutableListOf<ProvidedValue<*>>(LocalViewModelStoreOwner provides ownOwner)

        // Child: parent'ning contentKey'i bo'yicha AYNAN O'SHA store (bir xil kalit → bir xil store)
        val parentContentKey = entry.metadata[ParentContentKey]
        if (parentContentKey != null) {
            val parentOwner = rememberViewModelStoreOwner(
                key = parentContentKey,
                provider = viewModelStoreProvider,
                savedStateRegistryOwner = savedStateRegistryOwner,
            )
            providedValues += LocalSharedViewModelStoreOwner provides parentOwner
        }

        CompositionLocalProvider(*providedValues.toTypedArray()) { entry.Content() }
    },
) {
    companion object {
        /**
         * Child entry metadata'si: "mening ViewModel'larim egasi — [parentKey] entry'si".
         * Parent entry `clazzContentKey = { it.toContentKey() }` bilan e'lon qilingan bo'lishi shart.
         */
        fun parent(parentKey: NavKey): Map<String, Any> =
            metadata { put(ParentContentKey, parentKey.toContentKey()) }
    }
}

/** Metadata kaliti: parent entry'ning contentKey'i. */
private object ParentContentKey : NavMetadataKey<Any>

/**
 * Entry'ning contentKey'i. PARENT (`clazzContentKey`) ham, CHILD ([SharedViewModelStoreNavEntryDecorator.parent])
 * ham AYNAN SHU funksiyani ishlatadi — kalitlar mos kelishi kafolatlanadi.
 * `@Serializable data class` key'larda `toString()` barcha argumentlarni o'z ichiga oladi.
 */
fun NavKey.toContentKey(): String = toString()

/**
 * Parent entry'ning [ViewModelStoreOwner]i. Faqat [SharedViewModelStoreNavEntryDecorator.parent]
 * metadata'si bor entry ichida mavjud.
 */
val LocalSharedViewModelStoreOwner = staticCompositionLocalOf<ViewModelStoreOwner> {
    error(
        "LocalSharedViewModelStoreOwner yo'q: entry metadata'siga " +
            "SharedViewModelStoreNavEntryDecorator.parent(parentKey) qo'shilmagan",
    )
}

/**
 * [SharedViewModelStoreNavEntryDecorator]ni yaratadi va eslab qoladi.
 *
 * @param viewModelStoreOwner store'lar shu owner (Activity) ichida saqlanadi → rotation'dan omon
 *   qoladi. Default factory va CreationExtras ham undan olinadi (Hilt'ning factory'si shu yerdan).
 */
@Composable
fun <T : Any> rememberSharedViewModelStoreNavEntryDecorator(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "LocalViewModelStoreOwner topilmadi"
    },
): SharedViewModelStoreNavEntryDecorator<T> {
    // Har bir chaqiruv joyi (har bir stack) uchun alohida provider; Activity yo'q qilinganda tozalanadi
    val viewModelStoreProvider = rememberViewModelStoreProvider(parent = viewModelStoreOwner)
    return remember(viewModelStoreProvider) {
        SharedViewModelStoreNavEntryDecorator(viewModelStoreProvider)
    }
}
