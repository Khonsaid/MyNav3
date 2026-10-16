package uz.latizx.mynav3.core.navigation.deeplink

import androidx.navigation3.runtime.NavKey

/** Deep link: qaysi stack ochiladi + Back bosilsa ilovadan chiqilsinmi. */
data class DeepLinkTarget(
    val backStack: List<NavKey>,
    val exitOnBack: Boolean,
)