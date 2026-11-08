package com.TJPK.tapsiris

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.TJPK.tapsiris.data.local.OneCSettingsManager
import com.TJPK.tapsiris.data.local.UserManager
import com.TJPK.tapsiris.data.local.UserRole
import com.TJPK.tapsiris.data.mock.MockScheduleData
import com.TJPK.tapsiris.data.repository.ScheduleRepositoryImpl
import com.TJPK.tapsiris.domain.model.WeekType
import com.TJPK.tapsiris.notifications.NotificationHelper
import com.TJPK.tapsiris.presentation.auth.AuthScreen
import com.TJPK.tapsiris.presentation.schedule.ScheduleScreen
import com.TJPK.tapsiris.presentation.schedule.ScheduleScreenContent
import com.TJPK.tapsiris.presentation.schedule.ScheduleUiEvent
import com.TJPK.tapsiris.presentation.schedule.ScheduleUiState
import com.TJPK.tapsiris.presentation.schedule.ScheduleViewModel
import com.TJPK.tapsiris.presentation.settings.SettingsScreen
import com.TJPK.tapsiris.ui.theme.TapsirizTheme
import com.TJPK.tapsiris.utils.LanguageManager
import com.TJPK.tapsiris.utils.ThemeManager
import com.TJPK.tapsiris.utils.ThemeMode

/**
 * Экраны навигации приложения
 */
enum class AppScreen {
    AUTH,       // Экран входа / регистрации (3 роли: Студент, Преподаватель, Админ)
    SCHEDULE,   // Главный экран расписания
    SETTINGS    // Экран настроек (с пасхалкой 15 кликов на версию)
}

/**
 * =========================================================================================
 * СЛОЙ APP / ENTRY POINT: Главная активность приложения.
 * Инициализирует зависимости (UserManager, SettingsManager, Repository, ViewModel),
 * управляет языком, темой оформления, каналами уведомлений и переходами между экранами.
 * =========================================================================================
 */
class MainActivity : AppCompatActivity() {

    private val viewModel: ScheduleViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val settingsManager = OneCSettingsManager(applicationContext)
                val userManager = UserManager(applicationContext)
                val repository = ScheduleRepositoryImpl(settingsManager, applicationContext)
                return ScheduleViewModel(repository, settingsManager, userManager, applicationContext) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageManager.applySavedLanguage(this)
        ThemeManager.init(this)
        NotificationHelper.createNotificationChannels(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by ThemeManager.themeModeState.collectAsState()
            val themePalette by ThemeManager.themePaletteState.collectAsState()
            val isSystemDark = isSystemInDarkTheme()

            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            TapsirizTheme(darkTheme = isDark, palette = themePalette) {
                val userManager = remember { UserManager(applicationContext) }
                val settingsManager = remember { OneCSettingsManager(applicationContext) }
                val repository = remember { ScheduleRepositoryImpl(settingsManager, applicationContext) }

                var currentScreen by remember {
                    mutableStateOf(if (userManager.isLoggedIn()) AppScreen.SCHEDULE else AppScreen.AUTH)
                }
                var userProfile by remember {
                    mutableStateOf(userManager.getUserProfile())
                }
                val uiState by viewModel.uiState.collectAsState()

                // Обработка кнопки "Назад" в настройках
                BackHandler(enabled = currentScreen == AppScreen.SETTINGS) {
                    currentScreen = AppScreen.SCHEDULE
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                        when (screen) {
                            AppScreen.AUTH -> {
                                AuthScreen(
                                    groups = uiState.groups.ifEmpty { MockScheduleData.groups },
                                    onLoadGroups = { repository.getGroups() },
                                    onLoadGroupsFromDb = { repository.getGroupsFromDb() },
                                    onAuthenticate = { login, password, role ->
                                        repository.authenticate(login, password, role)
                                    },
                                    onAuthSuccess = { profile ->
                                        userManager.saveUser(profile)
                                        userProfile = profile
                                        viewModel.onEvent(ScheduleUiEvent.UpdateUserContext(profile))
                                        currentScreen = AppScreen.SCHEDULE
                                    },
                                    onGuestLogin = { groupName, groupId ->
                                        userManager.loginAsGuest(groupName, groupId)
                                        val profile = userManager.getUserProfile()
                                        userProfile = profile
                                        viewModel.onEvent(ScheduleUiEvent.UpdateUserContext(profile))
                                        currentScreen = AppScreen.SCHEDULE
                                    }
                                )
                            }

                            AppScreen.SCHEDULE -> {
                                ScheduleScreen(
                                    viewModel = viewModel,
                                    onOpenSettings = { currentScreen = AppScreen.SETTINGS }
                                )
                            }

                            AppScreen.SETTINGS -> {
                                SettingsScreen(
                                    userProfile = userProfile,
                                    serverConfig = uiState.serverConfig,
                                    onNavigateBack = { currentScreen = AppScreen.SCHEDULE },
                                    onLogout = {
                                        userManager.logout()
                                        val profile = userManager.getUserProfile()
                                        userProfile = profile
                                        viewModel.onEvent(ScheduleUiEvent.UpdateUserContext(profile))
                                        currentScreen = AppScreen.AUTH
                                    },
                                    onSaveServerConfig = {
                                        viewModel.onEvent(ScheduleUiEvent.UpdateServerConfig(it))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScheduleScreenPreview() {
    TapsirizTheme {
        ScheduleScreenContent(
            state = ScheduleUiState(
                groups = MockScheduleData.groups,
                selectedGroup = MockScheduleData.groups.first(),
                weekType = WeekType.ODD,
                selectedDayIndex = 2,
                weekSchedule = MockScheduleData.getWeekSchedule(WeekType.ODD),
                bells = MockScheduleData.bells,
                is1CConnected = true,
                lastUpdatedText = "Сегодня в 08:15"
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AuthScreenPreview() {
    TapsirizTheme {
        AuthScreen(
            groups = MockScheduleData.groups,
            onAuthSuccess = {},
            onGuestLogin = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ScheduleScreenDarkPreview() {
    TapsirizTheme(darkTheme = true) {
        ScheduleScreenContent(
            state = ScheduleUiState(
                groups = MockScheduleData.groups,
                selectedGroup = MockScheduleData.groups.first(),
                weekType = WeekType.ODD,
                selectedDayIndex = 2,
                weekSchedule = MockScheduleData.getWeekSchedule(WeekType.ODD),
                bells = MockScheduleData.bells,
                is1CConnected = false,
                lastUpdatedText = "Офлайн (кэш)"
            )
        )
    }
}
