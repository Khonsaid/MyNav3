package uz.latizx.mynav3.core.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/**
 * Har bir feature o'z ekranlarini (NavEntry) shu turdagi lambda orqali "o'rnatadi".
 *
 * ```
 * @Provides @IntoSet
 * fun provideCardsEntries(): EntryProviderInstaller = { navigator -> cardsEntries(navigator) }
 * ```
 *
 * Hilt barcha feature'larning installer'larini bitta `Set`ga yig'adi, [MainActivity] uni
 * oladi va `entryProvider { installers.forEach { install -> install(navigator) } }` qiladi.
 * Natijada `app` qatlami hech bir feature ekranini to'g'ridan-to'g'ri bilmaydi
 * (rasmiy nav3-recipes "modular/hilt" yondashuvi).
 *
 * Recipe'dan bitta farq: [Navigator] Hilt'dan inject qilinmaydi, PARAMETR sifatida beriladi.
 * Sababi — bizning back stack'larimiz `rememberNavBackStack` orqali saqlanadi va process
 * death'dan keyin tiklanadi. Hilt singletonidagi oddiy ro'yxat bunday qila olmasdi.
 */
typealias EntryProviderInstaller = EntryProviderScope<NavKey>.(navigator: Navigator) -> Unit
