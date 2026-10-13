package uz.latizx.mynav3.presenter.cards.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Kartalar tabi ildizi (pastki panel tabi). */
@Serializable
data object CardsKey : NavKey

/** Karta tafsiloti. */
@Serializable
data class CardDetailKey(val cardId: String) : NavKey

/** Shu karta ishlaydigan bankomatlar. */
@Serializable
data class CardATMKey(val cardId: String) : NavKey

/** Bankomat tafsiloti — pastdan chiqadigan "modal" ekran (custom animatsiya). */
@Serializable
data class AtmDetailKey(val atmId: String) : NavKey

/** DIALOG manzili: kartani bloklash / blokdan chiqarishni tasdiqlash. */
@Serializable
data class BlockCardDialogKey(val cardId: String, val block: Boolean) : NavKey

/** BOTTOM SHEET manzili: karta amallari. */
@Serializable
data class CardActionsSheetKey(val cardId: String, val isBlocked: Boolean) : NavKey

/**
 * NATIJA: BlockCardDialog → CardDetail. ResultEventBus kaliti — klass nomi
 * (`ResultEffect<BlockCardResult>` va `sendResult(BlockCardResult(...))`).
 */
data class BlockCardResult(val cardId: String, val block: Boolean)
