package com.TJPK.tapsiris.data.remote

/**
 * =========================================================================================
 * СЛОЙ DATA / REMOTE: Модель параметров подключения к серверу 1С.
 * =========================================================================================
 *
 * @property serverUrl Базовый URL опубликованного HTTP-сервиса (например: http://192.168.1.50/college/hs/schedule)
 * @property username1C Логин пользователя 1С для Basic Authentication
 * @property password1C Пароль пользователя 1С
 * @property isDemoMode Режим отказоустойчивости: если сервер 1С недоступен, использовать встроенный офлайн-кэш
 */
data class ServerConfig(
    val serverUrl: String = "http://192.168.1.50/college_base/hs/schedule",
    val username1C: String = "api_user",
    val password1C: String = "123456",
    val isDemoMode: Boolean = true
)
