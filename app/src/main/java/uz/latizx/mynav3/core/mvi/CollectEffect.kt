package uz.latizx.mynav3.core.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * ViewModel'dan keladigan bir martalik [Effect]larni xavfsiz yig'ish.
 *
 * Nega `STARTED`?
 * Navigation 3 sahna (scene) o'tish animatsiyasi paytida va ustida dialog/bottom sheet ochiq
 * turganda entry lifecycle'ini `STARTED`ga tushiradi (faqat to'liq joylashganda `RESUMED`).
 * `STARTED`da yig'sak — dialog ostidagi ekran ham natijaga reaksiya qila oladi, ekran fon
 * rejimiga o'tganda (STOPPED) esa effektlar Channel buferida kutib turadi.
 *
 * `Dispatchers.Main.immediate` — effekt yo'qolib qolmasligi uchun (lifecycle o'zgarishi bilan
 * bir vaqtda kelgan effekt darhol qayta ishlanadi).
 */
@Composable
fun <E> CollectEffect(
    effects: Flow<E>,
    onEffect: suspend (E) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnEffect by rememberUpdatedState(onEffect)

    LaunchedEffect(effects, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                effects.collect { effect -> currentOnEffect(effect) }
            }
        }
    }
}