package com.TJPK.tapciriz.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * =========================================================================================
 * СЛОЙ DATA / LOCAL: Менеджер учетной записи студента.
 * Сохраняет статус авторизации и профиль пользователя в SharedPreferences.
 * =========================================================================================
 */
data class UserProfile(
    val fullName: String,
    val login: String,
    val groupName: String,
    val isGuest: Boolean = false
)

class UserManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Проверяет, вошел ли пользователь в систему */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /** Получает текущий профиль пользователя */
    fun getUserProfile(): UserProfile {
        return UserProfile(
            fullName = prefs.getString(KEY_FULL_NAME, "Студент") ?: "Студент",
            login = prefs.getString(KEY_LOGIN, "student") ?: "student",
            groupName = prefs.getString(KEY_GROUP_NAME, "ИС-21") ?: "ИС-21",
            isGuest = prefs.getBoolean(KEY_IS_GUEST, false)
        )
    }

    /** Сохраняет успешный вход / регистрацию */
    fun saveUser(profile: UserProfile) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_FULL_NAME, profile.fullName)
            .putString(KEY_LOGIN, profile.login)
            .putString(KEY_GROUP_NAME, profile.groupName)
            .putBoolean(KEY_IS_GUEST, profile.isGuest)
            .apply()
    }

    /** Вход в гостевом режиме */
    fun loginAsGuest(defaultGroup: String = "ИС-21") {
        saveUser(
            UserProfile(
                fullName = "Гость колледжа",
                login = "guest",
                groupName = defaultGroup,
                isGuest = true
            )
        )
    }

    /** Выход из учетной записи */
    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "user_auth_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_LOGIN = "user_login"
        private const val KEY_GROUP_NAME = "user_group_name"
        private const val KEY_IS_GUEST = "is_guest"
    }
}
