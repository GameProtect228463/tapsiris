package com.TJPK.tapciriz.data.local

import android.content.Context
import android.content.SharedPreferences
import com.TJPK.tapciriz.data.remote.ServerConfig

/**
 * =========================================================================================
 * СЛОЙ DATA / LOCAL: Хранилище настроек приложения.
 * Сохраняет параметры подключения к серверу 1С в защищенном локальном SharedPreferences.
 * =========================================================================================
 */
class OneCSettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Считывает сохраненную конфигурацию подключения к 1С.
     */
    fun getConfig(): ServerConfig {
        return ServerConfig(
            serverUrl = prefs.getString(KEY_SERVER_URL, DEFAULT_URL) ?: DEFAULT_URL,
            username1C = prefs.getString(KEY_USERNAME, DEFAULT_USER) ?: DEFAULT_USER,
            password1C = prefs.getString(KEY_PASSWORD, DEFAULT_PASS) ?: DEFAULT_PASS,
            isDemoMode = prefs.getBoolean(KEY_DEMO_MODE, true)
        )
    }

    /**
     * Сохраняет новые параметры подключения к 1С.
     */
    fun saveConfig(config: ServerConfig) {
        prefs.edit()
            .putString(KEY_SERVER_URL, config.serverUrl.trim())
            .putString(KEY_USERNAME, config.username1C.trim())
            .putString(KEY_PASSWORD, config.password1C)
            .putBoolean(KEY_DEMO_MODE, config.isDemoMode)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "one_c_config_prefs"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username_1c"
        private const val KEY_PASSWORD = "password_1c"
        private const val KEY_DEMO_MODE = "is_demo_mode"

        private const val DEFAULT_URL = "http://192.168.1.50/college_base/hs/schedule"
        private const val DEFAULT_USER = "api_user"
        private const val DEFAULT_PASS = "123456"
    }
}
