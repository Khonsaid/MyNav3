package uz.latizx.mynav3.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.runtime.result.rememberResultEventBusNavEntryDecorator

/**
 * Ilovaning BUTUN navigatsiya holati (faqat ma'lumot). O'zgartirish — faqat [Navigator] orqali.
 *
 * Navigation 3'da back stack — bu oddiy, kuzatiladigan (snapshot) ro'yxat. Biz bir nechta
 * ro'yxat saqlaymiz:
 *
 * ```
 * authStack       : [Splash] → [Login, Otp]      — login oqimi (bo'sh = foydalanuvchi kirgan)
 * topLevelStack   : [Cards] yoki [Cards, Profile] — qaysi tablar "ochiq" (exit-through-home)
 * subStacks[Cards]: [Cards, CardDetail, CardATM]  — har bir tabning O'Z back stack'i
 * subStacks[Transfers], subStacks[Profile] ...
 * pendingDeepLink : login'dan keyin ochilishi kerak bo'lgan deep link (sintetik back stack)
 * ```
 *
 * Barcha ro'yxatlar [rememberNavBackStack] bilan yaratiladi → `@Serializable` NavKey'lar
 * Bundle'ga yoziladi va rotation / process death'dan keyin AVTOMATIK tiklanadi.
 */
@Stable
class NavigationState(
    /** "Uy" tab: exit-through-home — boshqa tab ildizida Back bosilsa shu tabga qaytiladi. */
    val homeKey: NavKey,
    val authStack: NavBackStack<NavKey>,
    val topLevelStack: NavBackStack<NavKey>,
    val subStacks: Map<NavKey, NavBackStack<NavKey>>,
    val pendingDeepLink: NavBackStack<NavKey>,
) {
    val topLevelKeys: Set<NavKey>
        get() = subStacks.keys

    /** authStack bo'sh emas → foydalanuvchi hali login qilmagan (yoki logout qilgan). */
    val isInAuthFlow: Boolean
        get() = authStack.isNotEmpty()

    /** Hozir tanlangan tab (auth oqimida — null). */
    val currentTopLevelKey: NavKey?
        get() = if (isInAuthFlow) null else topLevelStack.lastOrNull()

    /** Navigatsiya amallari qo'llanadigan joriy stack. */
    val currentStack: NavBackStack<NavKey>
        get() = if (isInAuthFlow) authStack else subStacks.getValue(currentTopLevelKey ?: homeKey)

    val currentKey: NavKey?
        get() = currentStack.lastOrNull()
}

/**
 * [NavigationState]ni yaratadi va saqlaydi.
 *
 * Boshlang'ich holat: `authStack = [authStartKey]`, tab stack'lari bo'sh. Ular login
 * muvaffaqiyatli bo'lgach [Navigator.completeAuthFlow] da to'ldiriladi.
 */
@Composable
fun rememberNavigationState(
    authStartKey: NavKey,
    homeKey: NavKey,
    topLevelKeys: Set<NavKey>,
): NavigationState {
    require(homeKey in topLevelKeys) { "homeKey ($homeKey) topLevelKeys ichida bo'lishi shart" }

    val authStack = rememberNavBackStack(authStartKey)
    val topLevelStack = rememberNavBackStack()
    // key(tabKey) — har bir stack'ning saqlangan holati (saveable kaliti) alohida bo'lishi uchun
    val subStacks = topLevelKeys.associateWith { tabKey -> key(tabKey) { rememberNavBackStack() } }
    val pendingDeepLink = rememberNavBackStack()

    return remember(homeKey, topLevelKeys) {
        NavigationState(
            homeKey = homeKey,
            authStack = authStack,
            topLevelStack = topLevelStack,
            subStacks = subStacks,
            pendingDeepLink = pendingDeepLink,
        )
    }
}

/**
 * Barcha stack'larni "bezatilgan" (decorated) [NavEntry] ro'yxatiga aylantiradi va
 * NavDisplay'ga beriladigan YAGONA ro'yxatni qaytaradi:
 * `auth entry'lari + (topLevelStack tartibida) tab entry'lari`.
 *
 * MUHIM: decorator'lar HAR BIR stack uchun alohida va DOIM (tab ko'rinmasa ham) chaqiriladi.
 * Shu tufayli:
 *  - tab almashtirilganda boshqa tabdagi ekranlarning ViewModel'lari va `rememberSaveable`
 *    holati yo'qolmaydi (multiple back stacks);
 *  - entry faqat O'Z stack'idan olib tashlanganda (pop) uning ViewModel'i tozalanadi;
 *  - logout'da tab stack'lari bo'shatiladi → barcha ekranlarning holati tozalanadi.
 *
 * Decorator'lar (tartib muhim — birinchisi eng tashqarida):
 *  1. SaveableStateHolder — `rememberSaveable` holatini entry bo'yicha saqlaydi va har bir
 *     entry'ga o'z SavedStateRegistryOwner'ini beradi (SavedStateHandle uchun);
 *  2. SharedViewModelStore — har bir entry'ga alohida ViewModelStore (hiltViewModel shu yerdan)
 *     + `parent(...)` metadata'li entry'ga parent'ning store'i (umumiy ViewModel);
 *  3. ResultEventBus — ekranlar orasida natija qaytarish (`ResultEffect`, `sendResult`).
 */
@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
    resultEventBus: ResultEventBus,
): List<NavEntry<NavKey>> {
    val authEntries = key(AUTH_STACK_COMPOSITION_KEY) {
        rememberDecoratedStack(authStack, entryProvider, resultEventBus)
    }
    val tabEntries: Map<NavKey, List<NavEntry<NavKey>>> = subStacks.mapValues { (tabKey, stack) ->
        key(tabKey) { rememberDecoratedStack(stack, entryProvider, resultEventBus) }
    }
    return authEntries + topLevelStack.flatMap { tabKey -> tabEntries[tabKey].orEmpty() }
}

@Composable
private fun rememberDecoratedStack(
    stack: NavBackStack<NavKey>,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
    resultEventBus: ResultEventBus,
): List<NavEntry<NavKey>> {
    val decorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
        // Kutubxonadagi rememberViewModelStoreNavEntryDecorator() o'rniga: xuddi shunday ishlaydi,
        // qo'shimcha ravishda bir nechta entry uchun UMUMIY ViewModel'ni qo'llab-quvvatlaydi.
        rememberSharedViewModelStoreNavEntryDecorator<NavKey>(),
        rememberResultEventBusNavEntryDecorator<NavKey>(resultEventBus),
    )
    return rememberDecoratedNavEntries(
        backStack = stack,
        entryDecorators = decorators,
        entryProvider = entryProvider,
    )
}

private const val AUTH_STACK_COMPOSITION_KEY = "auth-stack"
