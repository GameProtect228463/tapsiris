package com.TJPK.tapsiris.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.TJPK.tapsiris.R
import com.TJPK.tapsiris.ui.theme.DarkCyan
import com.TJPK.tapsiris.ui.theme.DeepPine
import com.TJPK.tapsiris.ui.theme.Mint
import com.TJPK.tapsiris.ui.theme.Purple40
import com.TJPK.tapsiris.utils.ThemeMode
import com.TJPK.tapsiris.utils.ThemePalette

/**
 * Диалог выбора режима темы (системная, светлая, тёмная)
 * и акцентной цветовой палитры приложения.
 */
@Composable
fun ThemeSelectDialog(
    currentThemeMode: ThemeMode,
    currentPalette: ThemePalette,
    onDismiss: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onPaletteSelected: (ThemePalette) -> Unit
) {
    val themeModes = listOf(
        ThemeMode.SYSTEM to stringResource(R.string.theme_system),
        ThemeMode.LIGHT to stringResource(R.string.theme_light),
        ThemeMode.DARK to stringResource(R.string.theme_dark)
    )

    val palettes = listOf(
        Triple(ThemePalette.DEFAULT, stringResource(R.string.palette_default), Purple40),
        Triple(ThemePalette.MINT, stringResource(R.string.palette_mint), Mint),
        Triple(ThemePalette.PINE, stringResource(R.string.palette_pine), DeepPine),
        Triple(ThemePalette.OCEAN, stringResource(R.string.palette_ocean), DarkCyan)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.theme_select_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Раздел: Режим темы
                Text(
                    text = stringResource(R.string.theme_mode_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                themeModes.forEach { (mode, label) ->
                    val isSelected = mode == currentThemeMode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeModeSelected(mode) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onThemeModeSelected(mode) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // Раздел: Цветовая палитра
                Text(
                    text = stringResource(R.string.theme_palette_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                palettes.forEach { (palette, label, color) ->
                    val isSelected = palette == currentPalette
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPaletteSelected(palette) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onPaletteSelected(palette) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.ok))
            }
        }
    )
}
