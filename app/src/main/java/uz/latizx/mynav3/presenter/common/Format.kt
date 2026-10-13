package uz.latizx.mynav3.presenter.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 12450000 → "12 450 000 so'm" */
fun formatSum(amount: Long): String {
    val grouped = amount.toString().reversed().chunked(3).joinToString(" ").reversed()
    return "$grouped so'm"
}

/** "8600123456781234" → "8600 1234 5678 1234" */
fun formatCardNumber(number: String): String = number.chunked(4).joinToString(" ")

/** "8600123456781234" → "8600 •••• •••• 1234" */
fun maskCardNumber(number: String): String = number.take(4) + " •••• •••• " + number.takeLast(4)

/** "901234567" → "+998 90 123 45 67" */
fun formatPhone(digits: String): String {
    if (digits.length != 9) return "+998 $digits"
    return "+998 ${digits.substring(0, 2)} ${digits.substring(2, 5)} ${digits.substring(5, 7)} ${digits.substring(7)}"
}

fun formatDateTime(millis: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(millis))

/** Masofa: 350 → "350 m", 1200 → "1.2 km" */
fun formatDistance(meters: Int): String =
    if (meters < 1_000) "$meters m" else String.format(Locale.US, "%.1f km", meters / 1_000.0)
