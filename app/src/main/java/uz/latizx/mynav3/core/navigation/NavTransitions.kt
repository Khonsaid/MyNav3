package uz.latizx.mynav3.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay

/**
 * Ekranlar orasidagi animatsiyalar.
 *
 * 2 daraja:
 *  1. GLOBAL — `NavDisplay(transitionSpec = ..., popTransitionSpec = ...,
 *     predictivePopTransitionSpec = ...)`. Barcha ekranlar uchun standart (o'ngdan surilish).
 *  2. ENTRY DARAJASIDA — `entry<Key>(metadata = NavTransitions.fade())`. Shu ekran uchun
 *     globalni bekor qiladi. NavDisplay qoidasi: oldinga o'tishda YANGI ekranning
 *     `TransitionKey`i, orqaga qaytishda YOPILAYOTGAN ekranning `PopTransitionKey`i olinadi.
 */
object NavTransitions {

    private const val DURATION_MS = 350

    // ---------------------------- Global (NavDisplay parametrlari) ----------------------------

    /** Oldinga: yangi ekran o'ngdan kiradi, eskisi biroz chapga suriladi. */
    val forward: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        slideInHorizontally(animationSpec = tween(DURATION_MS)) { fullWidth -> fullWidth } togetherWith
            slideOutHorizontally(animationSpec = tween(DURATION_MS)) { fullWidth -> -fullWidth / 4 }
    }

    /** Orqaga (tugma bilan): teskari yo'nalish. */
    val pop: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        slideInHorizontally(animationSpec = tween(DURATION_MS)) { fullWidth -> -fullWidth / 4 } togetherWith
            slideOutHorizontally(animationSpec = tween(DURATION_MS)) { fullWidth -> fullWidth }
    }

    /**
     * Predictive back (Android 13+ da barmoq bilan chetdan surish). Animatsiya progressi
     * barmoq harakatiga bog'lanadi — foydalanuvchi qaysi ekranga qaytishini oldindan ko'radi.
     * `swipeEdge` — qaysi chetdan surilgani (chap/o'ng).
     */
    val predictivePop: AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform = { _ ->
        slideInHorizontally { fullWidth -> -fullWidth / 4 } togetherWith
            slideOutHorizontally { fullWidth -> fullWidth }
    }

    // ---------------------------- Entry metadata ----------------------------

    /** Faqat "fade": tablar orasida, Splash/Login, natija ekrani. Tab almashishi "push"dek ko'rinmaydi. */
    fun fade(): Map<String, Any> = metadata {
        put(NavDisplay.TransitionKey) {
            fadeIn(tween(DURATION_MS)) togetherWith fadeOut(tween(DURATION_MS))
        }
        put(NavDisplay.PopTransitionKey) {
            fadeIn(tween(DURATION_MS)) togetherWith fadeOut(tween(DURATION_MS))
        }
        put(NavDisplay.PredictivePopTransitionKey) {
            fadeIn() togetherWith fadeOut()
        }
    }

    /**
     * "Modal" ekran: pastdan yuqoriga chiqadi, eski ekran joyida qoladi
     * (`ExitTransition.KeepUntilTransitionsFinished`). Yopilganda pastga tushadi.
     */
    fun slideUp(): Map<String, Any> = metadata {
        put(NavDisplay.TransitionKey) {
            slideInVertically(animationSpec = tween(DURATION_MS)) { fullHeight -> fullHeight } togetherWith
                ExitTransition.KeepUntilTransitionsFinished
        }
        put(NavDisplay.PopTransitionKey) {
            EnterTransition.None togetherWith
                slideOutVertically(animationSpec = tween(DURATION_MS)) { fullHeight -> fullHeight }
        }
        put(NavDisplay.PredictivePopTransitionKey) {
            EnterTransition.None togetherWith slideOutVertically { fullHeight -> fullHeight }
        }
    }
}
