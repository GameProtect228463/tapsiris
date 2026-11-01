package com.TJPK.tapsiris.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageManager {
    private const val PREFS_NAME = "settings_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    /**
     * Возвращает код текущего выбранного языка ("ru", "kk", "en" или "").
     */
    fun getCurrentLanguageCode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "") ?: ""
    }

    /**
     * Сохраняет выбор языка и применяет его через AppCompatDelegate.
     * AppCompatDelegate автоматически пересоздаёт Activity — recreate() не нужен.
     */
    fun setLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()

        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    /**
     * Восстанавливает сохранённый язык при старте приложения.
     * Вызывать до super.onCreate() в MainActivity.
     */
    fun applySavedLanguage(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = prefs.getString(KEY_LANGUAGE, "") ?: ""
        if (languageCode.isNotEmpty()) {
            val appLocale = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}
