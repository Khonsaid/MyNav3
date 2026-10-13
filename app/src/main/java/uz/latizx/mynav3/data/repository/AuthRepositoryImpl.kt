package uz.latizx.mynav3.data.repository

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import uz.latizx.mynav3.data.source.FakeBankDataSource
import uz.latizx.mynav3.domain.repository.AuthRepository

/**
 * Sessiya SharedPreferences'da saqlanadi → ilova qayta ochilganda Splash ekrani
 * foydalanuvchini to'g'ridan-to'g'ri asosiy ekranga o'tkazadi (auth oqimisiz).
 *
 * Demo: istalgan 9 xonali raqam, SMS kod — [DEMO_OTP].
 */
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val dataSource: FakeBankDataSource,
) : AuthRepository {

    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    override suspend fun isLoggedIn(): Boolean {
        dataSource.networkDelay(800)
        return prefs.getBoolean(KEY_LOGGED_IN, false)
    }

    override suspend fun sendOtp(phone: String): Result<Unit> {
        dataSource.networkDelay()
        return if (phone.length == 9) Result.success(Unit)
        else Result.failure(IllegalArgumentException("Telefon raqam 9 ta raqamdan iborat bo'lishi kerak"))
    }

    override suspend fun verifyOtp(phone: String, code: String): Result<Unit> {
        dataSource.networkDelay()
        if (code != DEMO_OTP) {
            return Result.failure(IllegalArgumentException("Kod noto'g'ri. Demo kod: $DEMO_OTP"))
        }
        prefs.edit { putBoolean(KEY_LOGGED_IN, true) }
        return Result.success(Unit)
    }

    override suspend fun logout() {
        dataSource.networkDelay(300)
        prefs.edit { putBoolean(KEY_LOGGED_IN, false) }
    }

    companion object {
        const val DEMO_OTP = "1234"
        private const val KEY_LOGGED_IN = "is_logged_in"
    }
}
