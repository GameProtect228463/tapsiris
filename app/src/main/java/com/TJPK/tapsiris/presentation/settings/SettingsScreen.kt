package com.TJPK.tapsiris.presentation.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.TJPK.tapsiris.R
import com.TJPK.tapsiris.data.local.NotificationSettingsManager
import com.TJPK.tapsiris.data.local.UserProfile
import com.TJPK.tapsiris.data.local.UserRole
import com.TJPK.tapsiris.data.remote.ServerConfig
import com.TJPK.tapsiris.notifications.NotificationHelper
import com.TJPK.tapsiris.presentation.components.ServerSettingsDialog
import com.TJPK.tapsiris.utils.LanguageManager
import com.TJPK.tapsiris.utils.ThemeManager
import com.TJPK.tapsiris.utils.ThemeMode
import com.TJPK.tapsiris.utils.ThemePalette
import kotlinx.coroutines.launch

/**
 * =========================================================================================
 * СЛОЙ PRESENTATION / SETTINGS: Экран настроек приложения.
 * Включает профиль пользователя (Студент, Преподаватель, Администратор из базы 1С),
 * смену языка, выбор темы оформления и палитры, управление уведомлениями,
 * параметры сервера 1С и информацию о приложении.
 * =========================================================================================
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfile: UserProfile,
    serverConfig: ServerConfig,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onSaveServerConfig: (ServerConfig) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Хранилище настроек уведомлений
    val notificationSettings = remember { NotificationSettingsManager(context) }

    // Состояния уведомлений
    var notifyBeforeLessons by remember { mutableStateOf(notificationSettings.isRemindersEnabled) }
    var notifyReplacements by remember { mutableStateOf(notificationSettings.isReplacementsEnabled) }
    var autoSyncSchedule by remember { mutableStateOf(true) }

    // Состояния диалогов
    var show1CDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    // Состояния темы и палитры
    var currentThemeMode by remember { mutableStateOf(ThemeManager.getThemeMode(context)) }
    var currentThemePalette by remember { mutableStateOf(ThemeManager.getThemePalette(context)) }

    // Языковые настройки
    var currentLangCode by remember { mutableStateOf(LanguageManager.getCurrentLanguageCode(context)) }

    val currentLangLabel by remember {
        derivedStateOf {
            when (currentLangCode) {
                "kk" -> "Қазақша"
                "en" -> "English"
                else -> "Русский"
            }
        }
    }

    val currentThemeModeLabel = when (currentThemeMode) {
        ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
        ThemeMode.LIGHT -> stringResource(R.string.theme_light)
        ThemeMode.DARK -> stringResource(R.string.theme_dark)
    }

    val currentThemePaletteLabel = when (currentThemePalette) {
        ThemePalette.DEFAULT -> stringResource(R.string.palette_default)
        ThemePalette.MINT -> stringResource(R.string.palette_mint)
        ThemePalette.PINE -> stringResource(R.string.palette_pine)
        ThemePalette.OCEAN -> stringResource(R.string.palette_ocean)
    }

    val currentThemeSubtitle = "$currentThemeModeLabel • $currentThemePaletteLabel"

    // Метка роли пользователя
    val roleLabel = when (userProfile.role) {
        UserRole.STUDENT -> stringResource(R.string.role_student)
        UserRole.TEACHER -> stringResource(R.string.role_teacher)
        UserRole.ADMINISTRATOR -> stringResource(R.string.role_admin)
    }

    // Иконка роли
    val roleIcon = when {
        userProfile.isGuest -> Icons.Default.School
        userProfile.role == UserRole.ADMINISTRATOR -> Icons.Default.AdminPanelSettings
        userProfile.role == UserRole.TEACHER -> Icons.Default.Badge
        else -> Icons.Default.Person
    }

    // Строки для Snackbar
    val testNotificationSentMsg = stringResource(R.string.notifications_test_sent)
    val permissionRequiredMsg = stringResource(R.string.notifications_permission_required)
    val permissionGrantedMsg = stringResource(R.string.notifications_permission_granted)
    val themeChangedMsg = stringResource(R.string.theme_changed)
    val serverSavedMsg = stringResource(R.string.server_settings_saved)

    // Лаунчер запроса разрешения на отправку уведомлений (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notifyBeforeLessons = true
            notificationSettings.isRemindersEnabled = true
            coroutineScope.launch {
                snackbarHostState.showSnackbar(permissionGrantedMsg)
            }
        } else {
            notifyBeforeLessons = false
            notificationSettings.isRemindersEnabled = false
            coroutineScope.launch {
                snackbarHostState.showSnackbar(permissionRequiredMsg)
            }
        }
    }

    // Счётчик кликов на версию приложения (Пасхалка)
    var versionClicksCount by remember { mutableIntStateOf(0) }
    var showEasterEggVideo by remember { mutableStateOf(false) }

    fun handleVersionClick() {
        val newClicks = versionClicksCount + 1
        if (newClicks >= 15) {
            versionClicksCount = 0
            showEasterEggVideo = true
        } else {
            versionClicksCount = newClicks
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Раздел: Профиль пользователя
            SettingsSectionTitle(title = stringResource(R.string.student_profile))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (userProfile.role) {
                                UserRole.ADMINISTRATOR -> MaterialTheme.colorScheme.errorContainer
                                UserRole.TEACHER -> MaterialTheme.colorScheme.tertiaryContainer
                                UserRole.STUDENT -> MaterialTheme.colorScheme.primaryContainer
                            },
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = roleIcon,
                                    contentDescription = null,
                                    tint = when (userProfile.role) {
                                        UserRole.ADMINISTRATOR -> MaterialTheme.colorScheme.error
                                        UserRole.TEACHER -> MaterialTheme.colorScheme.tertiary
                                        UserRole.STUDENT -> MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = userProfile.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (userProfile.role) {
                                        UserRole.ADMINISTRATOR -> MaterialTheme.colorScheme.errorContainer
                                        UserRole.TEACHER -> MaterialTheme.colorScheme.tertiaryContainer
                                        UserRole.STUDENT -> MaterialTheme.colorScheme.primaryContainer
                                    }
                                ) {
                                    Text(
                                        text = roleLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = when (userProfile.role) {
                                            UserRole.ADMINISTRATOR -> MaterialTheme.colorScheme.onErrorContainer
                                            UserRole.TEACHER -> MaterialTheme.colorScheme.onTertiaryContainer
                                            UserRole.STUDENT -> MaterialTheme.colorScheme.onPrimaryContainer
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            val infoText = when {
                                userProfile.isGuest -> stringResource(R.string.guest_access)
                                userProfile.role == UserRole.STUDENT ->
                                    "${stringResource(R.string.group)}: ${userProfile.groupName} • @${userProfile.login}"
                                userProfile.role == UserRole.TEACHER ->
                                    "${userProfile.department.ifBlank { "Преподаватель" }} • @${userProfile.login}"
                                userProfile.role == UserRole.ADMINISTRATOR ->
                                    "${userProfile.department.ifBlank { "Учебная часть (1С)" }} • @${userProfile.login}"
                                else -> "@${userProfile.login}"
                            }

                            Text(
                                text = infoText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = stringResource(R.string.auth_source_1c),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (userProfile.isGuest) stringResource(R.string.login_accaunt) else stringResource(R.string.out_accaunt),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Раздел: Интерфейс (Смена языка и темы оформления)
            SettingsSectionTitle(title = stringResource(R.string.interface_name))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column {
                    SettingsActionItem(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.app_language),
                        subtitle = currentLangLabel,
                        onClick = { showLanguageDialog = true }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    SettingsActionItem(
                        icon = Icons.Default.Palette,
                        title = stringResource(R.string.app_theme),
                        subtitle = currentThemeSubtitle,
                        onClick = { showThemeDialog = true }
                    )
                }
            }

            // Раздел: Уведомления
            SettingsSectionTitle(title = stringResource(R.string.notifications_section))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column {
                    SettingsSwitchItem(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.notifications_enable),
                        subtitle = stringResource(R.string.notifications_enable_desc),
                        checked = notifyBeforeLessons,
                        onCheckedChange = { isChecked ->
                            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !NotificationHelper.hasNotificationPermission(context)
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                notifyBeforeLessons = isChecked
                                notificationSettings.isRemindersEnabled = isChecked
                            }
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.NotificationsActive,
                        title = stringResource(R.string.notifications_replacements),
                        subtitle = stringResource(R.string.notifications_replacements_desc),
                        checked = notifyReplacements,
                        onCheckedChange = { isChecked ->
                            notifyReplacements = isChecked
                            notificationSettings.isReplacementsEnabled = isChecked
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    SettingsActionItem(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.notifications_send_test),
                        subtitle = stringResource(R.string.notifications_send_test_desc),
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !NotificationHelper.hasNotificationPermission(context)
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                val sent = NotificationHelper.sendTestNotification(context)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (sent) testNotificationSentMsg else permissionRequiredMsg
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // Раздел: Параметры расписания и синхронизации
            SettingsSectionTitle(title = stringResource(R.string.schedule_parameters))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column {
                    SettingsSwitchItem(
                        icon = Icons.Default.School,
                        title = stringResource(R.string.auto_sync),
                        subtitle = stringResource(R.string.auto_sync_subtitle),
                        checked = autoSyncSchedule,
                        onCheckedChange = { autoSyncSchedule = it }
                    )
                }
            }

            // Раздел: Подключение к серверу 1С
            SettingsSectionTitle(title = stringResource(R.string.integration_1c))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column {
                    SettingsActionItem(
                        icon = Icons.Default.Dns,
                        title = stringResource(R.string.server_params_1c),
                        subtitle = serverConfig.serverUrl.ifBlank { stringResource(R.string.address_not_configured) },
                        onClick = { show1CDialog = true }
                    )
                }
            }

            // Раздел: О приложении
            SettingsSectionTitle(title = stringResource(R.string.about_app))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column {
                    SettingsActionItem(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.app_title_full),
                        subtitle = stringResource(R.string.app_system_subtitle),
                        onClick = {}
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
                            .clickable { handleVersionClick() },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.app_version),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = stringResource(R.string.app_version_number),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Диалог выбора языка
    if (showLanguageDialog) {
        LanguageSelectDialog(
            currentCode = currentLangCode,
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { code ->
                currentLangCode = code
                LanguageManager.setLanguage(context, code)
            }
        )
    }

    // Диалог выбора темы и цветовой палитры
    if (showThemeDialog) {
        ThemeSelectDialog(
            currentThemeMode = currentThemeMode,
            currentPalette = currentThemePalette,
            onDismiss = { showThemeDialog = false },
            onThemeModeSelected = { mode ->
                currentThemeMode = mode
                ThemeManager.setThemeMode(context, mode)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(themeChangedMsg)
                }
            },
            onPaletteSelected = { palette ->
                currentThemePalette = palette
                ThemeManager.setThemePalette(context, palette)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(themeChangedMsg)
                }
            }
        )
    }

    // Диалог настройки 1С
    if (show1CDialog) {
        ServerSettingsDialog(
            currentConfig = serverConfig,
            onDismiss = { show1CDialog = false },
            onSave = {
                onSaveServerConfig(it)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(serverSavedMsg)
                }
            }
        )
    }

    // Диалог видео-пасхалки (15 нажатий)
    if (showEasterEggVideo) {
        EasterEggVideoDialog(
            onDismiss = { showEasterEggVideo = false }
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 6.dp)
    )
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
