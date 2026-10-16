package uz.latizx.mynav3.presenter.app

import androidx.annotation.DrawableRes
import androidx.navigation3.runtime.NavKey
import uz.latizx.mynav3.R
import uz.latizx.mynav3.presenter.cards.navigation.CardsKey
import uz.latizx.mynav3.presenter.profile.navigation.ProfileKey
import uz.latizx.mynav3.presenter.settings.navigation.SettingsKey
import uz.latizx.mynav3.presenter.transfer.navigation.TransfersKey

/**
 * Pastki panel (bottom navigation) tablari.
 * Har bir tab = alohida back stack ildizi (NavKey). Birinchisi — "uy" tab (exit-through-home).
 */
enum class TopLevelDestination(
    val key: NavKey,
    @param:DrawableRes val iconRes: Int,
    val label: String,
) {
    CARDS(CardsKey, R.drawable.ic_cards, "Kartalar"),
    TRANSFERS(TransfersKey, R.drawable.ic_transfer, "O'tkazmalar"),
    SETTINGS(SettingsKey, R.drawable.ic_location, "Sozlamalar"),
    PROFILE(ProfileKey, R.drawable.ic_profile, "Profil"),
}

/** Uy tab: ilovaga kirilganda shu ochiladi, boshqa tab ildizida Back bosilsa shu yerga qaytiladi. */
val HOME_KEY: NavKey = TopLevelDestination.CARDS.key

/** Tablar to'plami (tartib saqlanadi). O'zgarmas instance — `remember` kaliti sifatida xavfsiz. */
val TOP_LEVEL_KEYS: Set<NavKey> = TopLevelDestination.entries.map { it.key }.toSet()
