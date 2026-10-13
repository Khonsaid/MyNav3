package uz.latizx.mynav3.presenter.auth.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/*
 * NavKey — Navigation 3'da "manzil". Nav2'dagi route string'lar o'rniga oddiy Kotlin tiplari:
 *  - argumentsiz ekran   → `data object`
 *  - argumentli ekran    → `data class` (argumentlar tip-xavfsiz, string parse YO'Q)
 *  - `@Serializable`     → rememberNavBackStack ularni Bundle'ga saqlaydi (process death)
 *
 * Qoida: key'da faqat ID va kichik primitivlar. Katta obyektlar (Card, List...) emas —
 * ular ViewModel'da repository'dan olinadi.
 */

/** Ilova ochilganda sessiyani tekshiradigan ekran. */
@Serializable
data object SplashKey : NavKey

/** Telefon raqam kiritish. */
@Serializable
data object LoginKey : NavKey

/** SMS kodni tasdiqlash. Argument: telefon raqam. */
@Serializable
data class OtpKey(val phone: String) : NavKey
