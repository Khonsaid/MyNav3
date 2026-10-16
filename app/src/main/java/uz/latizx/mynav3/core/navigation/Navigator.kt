package uz.latizx.mynav3.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Ilovadagi BARCHA navigatsiya amallari shu yerda. [NavigationState]ni o'zgartiradi,
 * NavDisplay esa o'zgarishni avtomatik ko'rib ekranni almashtiradi.
 *
 * Oddiy Kotlin klassi (Compose'ga bog'liq emas) → unit test bilan to'liq tekshiriladi
 * (`app/src/test/.../NavigatorTest.kt`).
 *
 * | Holat                          | Metod                                  |
 * |--------------------------------|----------------------------------------|
 * | Oddiy push                     | [navigate]                             |
 * | Orqaga (system back, ← tugma)  | [goBack]                               |
 * | Joriy ekranni almashtirish     | [replaceCurrent] (Splash→Login, Form→Success) |
 * | Ma'lum ekrangacha qaytish      | [popUpTo]                              |
 * | Bottom navigation tablari      | [switchTab] (+ reselect → tab ildizi)  |
 * | Boshqa tabdagi ekranga o'tish  | [navigateInTab]                        |
 * | Login tugadi / logout          | [completeAuthFlow] / [logout]          |
 * | Deep link (login'gacha kutadi) | [openDeepLink]                         |
 *
 * Eslatma: [NavBackStack.removeLast] ishlatilmaydi — Kotlin 2.x + compileSdk 35+ da u Java 21
 * metodiga bog'lanib, Android 14 va undan pastda `NoSuchMethodError` beradi.
 * O'rniga `removeAt(lastIndex)` ishlatamiz.
 */
class Navigator(private val state: NavigationState) {

    // ------------------------------------------------------------------------------------
    // Oddiy navigatsiya
    // ------------------------------------------------------------------------------------

    /**
     * Joriy stack'ka [key] qo'shadi.
     *  - [key] top-level (tab) bo'lsa → [switchTab].
     *  - [key] allaqachon eng tepada bo'lsa → hech narsa qilmaydi ("singleTop", ikki marta
     *    tez bosishdan himoya).
     *  - [key] stack'da pastroqda bo'lsa → uning ustidagilarni olib tashlaydi ("clearTop").
     *    Bitta stack'da bir xil key ikki marta bo'lmasligi shart: `contentKey` takrorlansa,
     *    SaveableStateHolder xato beradi.
     */
    fun navigate(key: NavKey) {
        if (key in state.topLevelKeys) {
            switchTab(key)
            return
        }
        state.currentStack.pushSingleTop(key)
    }

    /**
     * Bitta qadam orqaga. NavDisplay `onBack`ni har bir olib tashlanadigan entry uchun
     * BIR MARTADAN chaqiradi (sahna bir vaqtda bir nechta entry'ni yopsa — har biri uchun
     * alohida) — shuning uchun bu metod har doim aynan bitta entry'ni olib tashlaydi.
     */
    fun goBack() {
        val stack = state.currentStack
        when {
            // 1) Joriy stack ichida orqaga
            stack.size > 1 -> stack.removeLastEntry()
            // 2) Tab ildizidamiz va bu "uy" tab emas → uy tabga qaytamiz (exit-through-home)
            !state.isInAuthFlow && state.topLevelStack.size > 1 -> state.topLevelStack.removeLastEntry()
            // 3) Uy tab ildizi yoki auth ildizi: NavDisplay bu holatda onBack'ni chaqirmaydi —
            //    tizim o'zi Activity'ni yopadi. Bu yerda hech narsa qilmaymiz.
            else -> Unit
        }
    }
    /**
     * Faqat TIZIM Back uchun (NavDisplay.onBack). Ekrandagi "←" tugmalar avvalgidek goBack()'ni chaqiradi.
     * @return false — ilovadan chiqish kerak (deep link bilan ochilgan ekrandamiz).
     */
    fun onSystemBack(): Boolean {
        val target = state.exitOnBackKey.lastOrNull()
        if (target != null && !state.isInAuthFlow && state.currentStack.lastOrNull() == target) {
            state.exitOnBackKey.clear()
            return false
        }
        goBack()
        return true
    }

    /**
     * Joriy (eng tepadagi) ekranni [key] bilan almashtiradi — orqaga qaytganda eski ekran
     * ko'rinmaydi. Misollar: Splash → Login, TransferForm → TransferSuccess,
     * bottom sheet → boshqa ekran.
     */
    fun replaceCurrent(key: NavKey) {
        val stack = state.currentStack
        val isTabRoot = !state.isInAuthFlow && stack.size <= 1
        if (isTabRoot) {
            // Tab ildizini almashtirib bo'lmaydi — oddiy push qilamiz
            navigate(key)
            return
        }
        stack.removeLastEntry()
        stack.pushSingleTop(key)
    }

    /**
     * Joriy stack'da [key]gacha qaytadi (uning ustidagi barcha ekranlar yopiladi).
     * [inclusive] = true bo'lsa [key]ning o'zi ham yopiladi (lekin stack ildizi hech qachon).
     *
     * @return [key] stack'da topilsa true.
     */
    fun popUpTo(key: NavKey, inclusive: Boolean = false): Boolean {
        val stack = state.currentStack
        val index = stack.lastIndexOf(key)
        if (index < 0) return false
        val targetSize = (if (inclusive) index else index + 1).coerceAtLeast(1)
        while (stack.size > targetSize) stack.removeLastEntry()
        return true
    }

    // ------------------------------------------------------------------------------------
    // Bottom navigation (multiple back stacks)
    // ------------------------------------------------------------------------------------

    /**
     * Tabga o'tish. Har bir tabning o'z stack'i bor va u saqlanib qoladi: Kartalar →
     * Karta tafsiloti → O'tkazmalar tabiga o'tib qaytsangiz, yana Karta tafsiloti ochiq turadi.
     *
     * Tanlangan tab yana bosilsa (reselect) → shu tab ildiziga qaytiladi.
     */
    fun switchTab(tab: NavKey) {
        require(tab in state.topLevelKeys) { "$tab top-level (tab) key emas" }
        if (state.isInAuthFlow) return
        state.exitOnBackKey.clear()

        if (tab == state.currentTopLevelKey) {
            state.subStacks.getValue(tab).popToRoot()
        } else {
            selectTab(tab)
        }
    }

    /**
     * Boshqa tabga o'tib, o'sha tab stack'iga [key]ni qo'shadi (cross-tab navigatsiya).
     * Misol: Karta tafsilotidan "Shu kartadan o'tkazma" → O'tkazmalar tabi + forma.
     * Orqaga bosilsa: forma → o'tkazmalar tarixi → Kartalar tabi (karta tafsiloti saqlangan).
     */
    fun navigateInTab(tab: NavKey, key: NavKey) {
        require(tab in state.topLevelKeys) { "$tab top-level (tab) key emas" }
        if (state.isInAuthFlow) return
        state.exitOnBackKey.clear()
        selectTab(tab)
        state.subStacks.getValue(tab).pushSingleTop(key)
    }

    // ------------------------------------------------------------------------------------
    // Auth oqimi (conditional navigation)
    // ------------------------------------------------------------------------------------

    /**
     * Login muvaffaqiyatli (yoki sessiya allaqachon bor). Auth stack tozalanadi, shuning uchun
     * asosiy ekrandan Back bosilganda Login/OTP ekranlariga QAYTILMAYDI.
     * Login'dan oldin kelgan deep link bo'lsa — endi ochiladi.
     */
    fun completeAuthFlow() {
        if (!state.isInAuthFlow) return // ikki marta chaqirilsa ham xavfsiz (idempotent)
        resetMainStacks()
        val deepLink = state.pendingDeepLink.toList()
        state.pendingDeepLink.clear()
        if (deepLink.isNotEmpty()) applyDeepLink(deepLink)
        state.authStack.clear()
    }

    /**
     * Logout: barcha tab stack'lari BO'SHATILADI → ulardagi barcha ekranlarning ViewModel'lari
     * va saqlangan holati tozalanadi (keyingi foydalanuvchi oldingisining ma'lumotini ko'rmaydi).
     */
    fun logout(loginKey: NavKey) {
        state.pendingDeepLink.clear()
        state.exitOnBackKey.clear()
        state.topLevelStack.clear()
        state.subStacks.values.forEach { stack -> stack.clear() }
        state.authStack.clear()
        state.authStack.add(loginKey)
    }

    // ------------------------------------------------------------------------------------
    // Deep link
    // ------------------------------------------------------------------------------------

    /**
     * Deep link'dan kelgan sintetik back stack'ni ochadi, masalan `[CardsKey, CardDetailKey("2")]`.
     * Foydalanuvchi hali login qilmagan bo'lsa — deep link saqlanadi va login'dan keyin
     * ([completeAuthFlow]) ochiladi.
     */
    fun openDeepLink(backStack: List<NavKey>, exitOnBack: Boolean = false) {
        if (backStack.isEmpty()) return
        // Har bir yangi havola eski belgini o'chiradi; kerak bo'lsa yangisini qo'yadi
        state.exitOnBackKey.clear()
        if (exitOnBack) state.exitOnBackKey.add(backStack.last())

        if (state.isInAuthFlow) {
            state.pendingDeepLink.clear()
            state.pendingDeepLink.addAll(backStack)
        } else {
            applyDeepLink(backStack)
        }
    }

    // ------------------------------------------------------------------------------------
    // Ichki yordamchilar
    // ------------------------------------------------------------------------------------

    /** Exit-through-home: topLevelStack = [home] yoki [home, tab]. */
    private fun selectTab(tab: NavKey) {
        state.topLevelStack.clear()
        state.topLevelStack.add(state.homeKey)
        if (tab != state.homeKey) state.topLevelStack.add(tab)

        val tabStack = state.subStacks.getValue(tab)
        if (tabStack.isEmpty()) tabStack.add(tab)
    }

    /** Har bir tab stack'i = [tab ildizi], tanlangan tab = home. */
    private fun resetMainStacks() {
        state.subStacks.forEach { (tab, stack) ->
            stack.clear()
            stack.add(tab)
        }
        state.topLevelStack.clear()
        state.topLevelStack.add(state.homeKey)
    }

    private fun applyDeepLink(backStack: List<NavKey>) {
        // Sintetik back stack tab ildizi bilan boshlanmasa — uy tabga joylaymiz
        val normalized = if (backStack.first() in state.topLevelKeys) {
            backStack
        } else {
            listOf(state.homeKey) + backStack
        }
        val tab = normalized.first()
        selectTab(tab)
        val tabStack = state.subStacks.getValue(tab)
        tabStack.clear()
        tabStack.addAll(normalized.distinct())
    }
}

/** `removeLast()` o'rniga (Android 14 va pastda xavfsiz). */
private fun NavBackStack<NavKey>.removeLastEntry() {
    removeAt(lastIndex)
}

/** singleTop + clearTop: bir xil key stack'da ikki marta bo'lmaydi. */
private fun NavBackStack<NavKey>.pushSingleTop(key: NavKey) {
    val index = indexOf(key)
    if (index < 0) {
        add(key)
        return
    }
    while (lastIndex > index) removeLastEntry()
}

private fun NavBackStack<NavKey>.popToRoot() {
    while (size > 1) removeLastEntry()
}
