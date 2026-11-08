package com.TJPK.tapsiris.utils

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Режим темы приложения (системная, светлая, тёмная).
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

/**
 * Цветовая палитра оформления приложения.
 */
enum class ThemePalette {
    DEFAULT, // Фиолетово-индиго палитра по умолчанию
    MINT,    // Мятно-изумрудная гамма (Mint)
    PINE,    // Лесная хвоя (Pine / Forest)
    OCEAN    // Океанический циан (Ocean Cyan)
}

/**
 * Менеджер управления темой и цветовой палитрой оформления приложения.
 * Сохраняет настройки в SharedPreferences и предоставляет реактивные StateFlow
 * для мгновенного обновления Compose UI без пересоздания активности.
 */
object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "selected_theme_mode"
    private const val KEY_THEME_PALETTE = "selected_theme_palette"

    private val _themeModeState = MutableStateFlow(ThemeMode.SYSTEM)
    val themeModeState: StateFlow<ThemeMode> = _themeModeState.asStateFlow()

    private val _themePaletteState = MutableStateFlow(ThemePalette.DEFAULT)
    val themePaletteState: StateFlow<ThemePalette> = _themePaletteState.asStateFlow()

    private var isInitialized = false

    /**
     * Инициализирует состояния темы из SharedPreferences при старте приложения.
     */
    fun init(context: Context) {
        if (isInitialized) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val modeStr = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val paletteStr = prefs.getString(KEY_THEME_PALETTE, ThemePalette.DEFAULT.name) ?: ThemePalette.DEFAULT.name

        _themeModeState.value = try {
            ThemeMode.valueOf(modeStr)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }

        _themePaletteState.value = try {
            ThemePalette.valueOf(paletteStr)
        } catch (_: Exception) {
            ThemePalette.DEFAULT
        }
        isInitialized = true
    }

    /**
     * Получить текущий режим темы.
     */
    fun getThemeMode(context: Context): ThemeMode {
        init(context)
        return _themeModeState.value
    }

    /**
     * Сохранить и применить новый режим темы.
     */
    fun setThemeMode(context: Context, mode: ThemeMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeModeState.value = mode
    }

    /**
     * Получить текущую цветовую палитру.
     */
    fun getThemePalette(context: Context): ThemePalette {
        init(context)
        return _themePaletteState.value
    }

    /**
     * Сохранить и применить новую цветовую палитру.
     */
    fun setThemePalette(context: Context, palette: ThemePalette) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_PALETTE, palette.name).apply()
        _themePaletteState.value = palette
    }
}
