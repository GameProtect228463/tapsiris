package com.TJPK.tapsiris.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Тип (роль) учетной записи в системе колледжа (1С).
 */
enum class UserRole(val code: String) {
    STUDENT("student"),           // Студент
    TEACHER("teacher"),           // Преподаватель
    ADMINISTRATOR("admin")       // Администратор
}

/**
 * =========================================================================================
 * СЛОЙ DATA / LOCAL: Профиль пользователя и менеджер сессии.
 * Сохраняет статус авторизации, профиль и роль (Студент, Преподаватель, Администратор).
 * =========================================================================================
 */
data class UserProfile(
    val fullName: String,
    val login: String,
    val role: UserRole = UserRole.STUDENT,
    val groupName: String = "",
    /** UUID группы из справочника 1С (нужен для запроса расписания по groupId) */
    val groupId: String = "",
    val department: String = "",
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
        val roleStr = prefs.getString(KEY_ROLE, UserRole.STUDENT.name) ?: UserRole.STUDENT.name
        val role = try {
            UserRole.valueOf(roleStr)
        } catch (_: Exception) {
            UserRole.STUDENT
        }

        return UserProfile(
            fullName = prefs.getString(KEY_FULL_NAME, "Студент") ?: "Студент",
            login = prefs.getString(KEY_LOGIN, "student") ?: "student",
            role = role,
            groupName = prefs.getString(KEY_GROUP_NAME, "") ?: "",
            groupId = prefs.getString(KEY_GROUP_ID, "") ?: "",
            department = prefs.getString(KEY_DEPARTMENT, "") ?: "",
            isGuest = prefs.getBoolean(KEY_IS_GUEST, false)
        )
    }

    /** Сохраняет успешный вход / регистрацию */
    fun saveUser(profile: UserProfile) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_FULL_NAME, profile.fullName)
            .putString(KEY_LOGIN, profile.login)
            .putString(KEY_ROLE, profile.role.name)
            .putString(KEY_GROUP_NAME, profile.groupName)
            .putString(KEY_GROUP_ID, profile.groupId)
            .putString(KEY_DEPARTMENT, profile.department)
            .putBoolean(KEY_IS_GUEST, profile.isGuest)
            .apply()
    }

    /** Вход в гостевом режиме */
    fun loginAsGuest(defaultGroup: String = "", defaultGroupId: String = "") {
        saveUser(
            UserProfile(
                fullName = "Гость колледжа",
                login = "guest",
                role = UserRole.STUDENT,
                groupName = defaultGroup,
                groupId = defaultGroupId,
                department = "Студенческое отделение",
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
        private const val KEY_ROLE = "user_role"
        private const val KEY_GROUP_NAME = "user_group_name"
        private const val KEY_GROUP_ID = "user_group_id"
        private const val KEY_DEPARTMENT = "user_department"
        private const val KEY_IS_GUEST = "is_guest"
    }
}
