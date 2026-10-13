package uz.latizx.mynav3.core.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * MVI uchun bazaviy ViewModel.
 *
 * Har bir ekran o'z kontraktini e'lon qiladi:
 * ```
 * object CardATMScreenContract {
 *     sealed interface Intent    // UI → ViewModel : foydalanuvchi niyati (bosish, matn kiritish)
 *     data class UIState(...)    // ViewModel → UI : ekranning to'liq holati (StateFlow)
 *     sealed interface Effect    // ViewModel → UI : bir martalik hodisa (navigatsiya, xabar)
 * }
 * ```
 *
 * Oqim (unidirectional data flow):
 * ```
 *  UI ──onIntent(Intent)──▶ handleIntent() ──setState{}──▶ uiState ──▶ UI
 *                                  └──────sendEffect()──▶ effect ──▶ Route ──▶ Navigator
 * ```
 *
 * NAVIGATSIYA QOIDASI: ViewModel Navigator'ni ham, NavKey'larni ham BILMAYDI.
 * U faqat `Effect` yuboradi (masalan `Effect.OpenAtmDetail(id)`), navigatsiya qatlami
 * (`XNavigation.kt`) esa effektni aniq NavKey'ga aylantiradi. Natijada:
 *  - ViewModel'ni oddiy unit test bilan tekshirish mumkin;
 *  - ekranni boshqa oqimda qayta ishlatish oson (navigatsiya tashqaridan beriladi).
 */

abstract class BaseViewModel<I : Any, S : Any, E : Any>(initialState: S) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    /**
     * Bir martalik effektlar uchun `Channel` (SharedFlow emas), chunki:
     *  - effekt faqat BIR marta qayta ishlanadi (rotation'dan keyin takrorlanmaydi);
     *  - ekran vaqtincha STOPPED bo'lsa, effekt yo'qolmaydi — buferda kutib turadi.
     */
    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    protected val currentState: S
        get() = _uiState.value

    /** UI'dan keladigan YAGONA kirish nuqtasi. */
    fun onIntent(intent: I) {
        handleIntent(intent)
    }

    protected abstract fun handleIntent(intent: I)

    /** Holatni o'zgartirish: `setState { copy(isLoading = true) }` */
    protected fun setState(reducer: S.() -> S) {
        _uiState.update { it.reducer() }
    }

    protected fun sendEffect(effect: E) {
        _effect.trySend(effect)
    }
}
