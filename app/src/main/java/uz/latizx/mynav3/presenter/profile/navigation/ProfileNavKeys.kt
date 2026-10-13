package uz.latizx.mynav3.presenter.profile.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import uz.latizx.mynav3.domain.model.AppLanguage

/** Profil tabi ildizi. */
@Serializable
data object ProfileKey : NavKey

/** BOTTOM SHEET: tilni tanlash. Argument — joriy til (enum ham serializatsiya bo'ladi). */
@Serializable
data class LanguageSheetKey(val current: AppLanguage) : NavKey {
    companion object {
        /**
         * Natija uchun ANIQ kalit (klass nomi o'rniga). `AppLanguage` kabi umumiy tip boshqa
         * joyda ham natija sifatida yuborilishi mumkin bo'lsa — to'qnashuv bo'lmasligi uchun.
         */
        const val RESULT_KEY = "uz.latizx.navigation3.LanguageSheet.result"
    }
}

/** DIALOG: chiqishni tasdiqlash. */
@Serializable
data object LogoutDialogKey : NavKey

/** NATIJA: LogoutDialog → Profile. */
data object LogoutConfirmedResult
