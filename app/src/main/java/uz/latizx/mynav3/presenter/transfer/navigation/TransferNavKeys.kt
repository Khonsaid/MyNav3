package uz.latizx.mynav3.presenter.transfer.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** O'tkazmalar tabi ildizi: o'tkazmalar tarixi. */
@Serializable
data object TransfersKey : NavKey

/**
 * O'tkazma formasi — o'tkazma OQIMINING PARENT'i: oqimning umumiy ViewModel'i
 * (TransferFormViewModel) shu entry'ning ViewModelStore'ida yashaydi.
 *
 * Ixtiyoriy (default'li) argumentlar:
 *  - `fromCardId` — Karta tafsilotidan kelinganda (cross-tab) yoki "Takrorlash"da;
 *  - `recipient`  — deep link'dan (`nav3bank://transfer?to=...`) yoki "Takrorlash"da.
 * Deep link'da URI query nomlari (to/from) shu maydonlarga `{recipient}`/`{fromCardId}` orqali bog'lanadi.
 */
@Serializable
data class TransferFormKey(
    val fromCardId: String? = null,
    val recipient: String? = null,
) : NavKey

/**
 * CHILD (to'liq ekran): kartani tanlash. Tanlov natija sifatida QAYTARILMAYDI — u to'g'ridan-to'g'ri
 * oqimning umumiy ViewModel'iga yoziladi.
 *
 * @param form parent'ning key'i: (1) parent entry'ni topish uchun (metadata), (2) process
 *   death'dan keyin umumiy ViewModel'ni xuddi parent kabi yaratish uchun.
 * @param selectedCardId ro'yxatda belgilab turiladigan karta (ekranning O'Z ViewModel'i uchun)
 */
@Serializable
data class SelectCardKey(
    val form: TransferFormKey,
    val selectedCardId: String?,
) : NavKey

/**
 * CHILD (dialog): o'tkazmani tasdiqlash. Summa, karta va qabul qiluvchi key'da TASHILMAYDI —
 * dialog ularni umumiy ViewModel holatidan o'qiydi va "Tasdiqlash"ni o'sha ViewModel'ga yuboradi.
 */
@Serializable
data class TransferConfirmKey(val form: TransferFormKey) : NavKey

/** Muvaffaqiyat ekrani — forma O'RNIGA ochiladi (replaceCurrent), Back formaga qaytarmaydi. */
@Serializable
data class TransferSuccessKey(val transferId: String, val amount: Long) : NavKey
