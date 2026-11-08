package com.TJPK.tapsiris.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.TJPK.tapsiris.utils.ThemePalette

// -------------------------------------------------------------
// DEFAULT (Classic Indigo / Purple)
// -------------------------------------------------------------
private val DefaultDarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val DefaultLightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// -------------------------------------------------------------
// MINT (Изумрудно-мятная)
// -------------------------------------------------------------
private val MintLightColorScheme = lightColorScheme(
    primary = Mint,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F0E3),
    onPrimaryContainer = Color(0xFF093922),
    secondary = MintDark,
    onSecondary = Color.White,
    tertiary = Claret,
    onTertiary = Color.White,
    background = WhiteSmoke,
    surface = Color.White,
    onBackground = Color(0xFF131D19),
    onSurface = Color(0xFF131D19),
    surfaceVariant = Color(0xFFD7E4DE),
    onSurfaceVariant = Color(0xFF3E4D46)
)

private val MintDarkColorScheme = darkColorScheme(
    primary = MintLight,
    onPrimary = Color(0xFF073822),
    primaryContainer = MintDark,
    onPrimaryContainer = Color(0xFFD6F0E3),
    secondary = Mint,
    onSecondary = Color(0xFF003822),
    tertiary = Pink80,
    onTertiary = Color(0xFF492532),
    background = Color(0xFF101915),
    surface = Color(0xFF16231E),
    onBackground = Color(0xFFE0EAE5),
    onSurface = Color(0xFFE0EAE5),
    surfaceVariant = Color(0xFF23352D),
    onSurfaceVariant = Color(0xFFC0CEC7)
)

// -------------------------------------------------------------
// PINE (Лесная хвоя)
// -------------------------------------------------------------
private val PineLightColorScheme = lightColorScheme(
    primary = DeepPine,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E9CB),
    onPrimaryContainer = Color(0xFF152A07),
    secondary = Americana,
    onSecondary = Color.White,
    tertiary = DarkImperialBlue,
    onTertiary = Color.White,
    background = Color(0xFFF7F8F4),
    surface = Color.White,
    onBackground = Color(0xFF191D16),
    onSurface = Color(0xFF191D16),
    surfaceVariant = Color(0xFFDEE5D7),
    onSurfaceVariant = Color(0xFF424A3D)
)

private val PineDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9EC783),
    onPrimary = Color(0xFF1E370C),
    primaryContainer = DeepPine,
    onPrimaryContainer = Color(0xFFD9E9CB),
    secondary = Americana,
    onSecondary = Color.White,
    tertiary = Fossilised,
    onTertiary = Color(0xFF242621),
    background = GuerrillaForest,
    surface = Color(0xFF1F3830),
    onBackground = Color(0xFFE1EAE5),
    onSurface = Color(0xFFE1EAE5),
    surfaceVariant = Color(0xFF2C493F),
    onSurfaceVariant = Color(0xFFC1CDC7)
)

// -------------------------------------------------------------
// OCEAN (Океанический циан)
// -------------------------------------------------------------
private val OceanLightColorScheme = lightColorScheme(
    primary = DarkCyan,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7EAE9),
    onPrimaryContainer = Color(0xFF0F2B2A),
    secondary = Americana,
    onSecondary = Color.White,
    tertiary = EverGreen,
    onTertiary = Color.White,
    background = AlabasterGrey,
    surface = Color.White,
    onBackground = Color(0xFF171D1C),
    onSurface = Color(0xFF171D1C),
    surfaceVariant = Color(0xFFD6E2E0),
    onSurfaceVariant = Color(0xFF3F4B49)
)

private val OceanDarkColorScheme = darkColorScheme(
    primary = DarkCyanLight,
    onPrimary = Color(0xFF0D2B2A),
    primaryContainer = DarkCyan,
    onPrimaryContainer = Color(0xFFD7EAE9),
    secondary = Americana,
    onSecondary = Color.White,
    tertiary = AlabasterGrey,
    onTertiary = Color(0xFF1F2625),
    background = EverGreen,
    surface = Color(0xFF143B2E),
    onBackground = Color(0xFFE0EAE6),
    onSurface = Color(0xFFE0EAE6),
    surfaceVariant = Color(0xFF1F4E3D),
    onSurfaceVariant = Color(0xFFC2D2CB)
)

@Composable
fun TapsirizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: ThemePalette = ThemePalette.DEFAULT,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        palette == ThemePalette.MINT -> if (darkTheme) MintDarkColorScheme else MintLightColorScheme
        palette == ThemePalette.PINE -> if (darkTheme) PineDarkColorScheme else PineLightColorScheme
        palette == ThemePalette.OCEAN -> if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
        darkTheme -> DefaultDarkColorScheme
        else -> DefaultLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
