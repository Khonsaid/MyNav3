package uz.latizx.mynav3.presenter.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.result.rememberResultEventBus
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.flow.Flow
import uz.latizx.mynav3.core.navigation.EntryProviderInstaller
import uz.latizx.mynav3.core.navigation.LocalSharedTransitionScope
import uz.latizx.mynav3.core.navigation.Navigator
import uz.latizx.mynav3.core.navigation.rememberNavigationState
import uz.latizx.mynav3.core.navigation.toEntries
import uz.latizx.mynav3.core.navigation.BottomSheetSceneStrategy
import uz.latizx.mynav3.core.navigation.NavTransitions
import uz.latizx.mynav3.core.navigation.shouldShowBottomBar
import uz.latizx.mynav3.presenter.auth.navigation.SplashKey


/**
 * Ilovaning ildiz Composable'i — Navigation 3 shu yerda yig'iladi.
 *
 * ```
 *  NavigationState (back stack'lar) ──toEntries()──▶ List<NavEntry> ──▶ NavDisplay
 *        ▲                                                │
 *        └──────────── Navigator (navigate/goBack) ◀──────┘ (onBack, ekranlar)
 * ```
 *
 * Ekran tuzilishi (tashqaridan ichkariga):
 * ```
 * Scaffold                     pastki panel (NavigationBar) — faqat tab ildizlarida
 *  └─ SharedTransitionLayout   ekranlar orasida "uchib o'tadigan" elementlar uchun
 *      └─ NavDisplay           sahnalar: bitta ekran / dialog / bottom sheet
 * ```
 *
 * Navigation 2'dan asosiy farq: NavController/NavGraph YO'Q. Back stack — oddiy ro'yxat,
 * uni o'zimiz boshqaramiz; NavDisplay esa shunchaki ro'yxatni ekranga aylantiradi.
 */

@Composable
fun Nav3AppContent(
    entryInstallers: Set<EntryProviderInstaller>,
    deepLinks: Flow<List<NavKey>>,
) {
    // 1) Holat: barcha back stack'lar (rotation va process death'dan keyin tiklanadi)
    val navigationState = rememberNavigationState(
        authStartKey = SplashKey,
        homeKey = HOME_KEY,
        topLevelKeys = TOP_LEVEL_KEYS,
    )

    // 2) Navigator: holatni o'zgartiruvchi yagona joy
    val navigator = remember(navigationState) { Navigator(navigationState) }

    // 3) Deep link'lar (cold start + ilova ochiq turganda kelganlari)
    LaunchedEffect(navigator, deepLinks) {
        deepLinks.collect { backStack -> navigator.openDeepLink(backStack) }
    }

    // 4) entryProvider: NavKey → NavEntry (ekran). Har bir feature o'z qismini o'rnatadi.
    val entryProvider = remember(navigator, entryInstallers) {
        entryProvider<NavKey> {
            entryInstallers.forEach { install -> this.install(navigator) }
        }
    }

    // 5) Ekranlar orasida natija qaytarish uchun umumiy "shina" (ResultEffect / sendResult)
    val resultEventBus = rememberResultEventBus()

    // 6) Back stack'lar → decorator'lar bilan bezatilgan entry'lar
    val entries = navigationState.toEntries(entryProvider, resultEventBus)

    // 7) Scene strategiyalari: dialog va bottom sheet oldingi ekran USTIDA ko'rsatiladi.
    //    Hech biri mos kelmasa — NavDisplay o'zi SinglePaneSceneStrategy (bitta ekran) ishlatadi.
    val dialogStrategy = remember { DialogSceneStrategy<NavKey>() }
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavKey>() }

    // 8) Pastki panel: login oqimida yo'q, asosiy oqimda faqat tab ildizlarida
    //    (detal ekranlar butun ekranni egallaydi).
    val showBottomBar = !navigationState.isInAuthFlow && entries.shouldShowBottomBar()

    Scaffold(
        // Insets'ni ichki ekranlar (o'z Scaffold/TopAppBar'lari) o'zi boshqaradi
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                AppBottomBar(
                    currentTab = navigationState.currentTopLevelKey,
                    onTabSelected = navigator::switchTab,
                )
            }
        },
    ) { innerPadding ->
        SharedTransitionLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            // Ekranlardagi Modifier.navSharedElement(...) shu scope'ni ishlatadi
            CompositionLocalProvider(LocalSharedTransitionScope provides this@SharedTransitionLayout) {
                NavDisplay(
                    entries = entries,
                    // Tizim Back / predictive back → Navigator. Har bir yopiladigan entry uchun bir marta.
                    onBack = navigator::goBack,
                    modifier = Modifier.fillMaxSize(),
                    sceneStrategies = listOf(dialogStrategy, bottomSheetStrategy),
                    transitionSpec = NavTransitions.forward,
                    popTransitionSpec = NavTransitions.pop,
                    predictivePopTransitionSpec = NavTransitions.predictivePop,
                )
            }
        }

    }
}
@Composable
private fun AppBottomBar(
    currentTab: NavKey?,
    onTabSelected: (NavKey) -> Unit,
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = destination.key == currentTab,
                // Tanlangan tab yana bosilsa → Navigator.switchTab tab ildiziga qaytaradi
                onClick = { onTabSelected(destination.key) },
                icon = {
                    Icon(painter = painterResource(destination.iconRes), contentDescription = null)
                },
                label = { Text(destination.label) },
            )
        }
    }
}










