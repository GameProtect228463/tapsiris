package com.TJPK.tapsiris.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Локальное хранилище настроек уведомлений расписания в SharedPreferences.
 */
class NotificationSettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "notification_prefs"
        private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        private const val KEY_REPLACEMENTS_ENABLED = "replacements_enabled"
        private const val KEY_MINUTES_BEFORE = "minutes_before"
    }

    /**
     * Включены ли напоминания о предстоящих парах.
     */
    var isRemindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMINDERS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, value).apply()

    /**
     * Включены ли оповещения о заменах в расписании.
     */
    var isReplacementsEnabled: Boolean
        get() = prefs.getBoolean(KEY_REPLACEMENTS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_REPLACEMENTS_ENABLED, value).apply()

    /**
     * За сколько минут до начала пары присылать напоминание.
     */
    var minutesBeforeLesson: Int
        get() = prefs.getInt(KEY_MINUTES_BEFORE, 15)
        set(value) = prefs.edit().putInt(KEY_MINUTES_BEFORE, value).apply()
}
