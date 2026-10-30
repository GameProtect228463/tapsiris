package com.TJPK.tapciriz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.TJPK.tapciriz.data.local.OneCSettingsManager
import com.TJPK.tapciriz.data.mock.MockScheduleData
import com.TJPK.tapciriz.data.repository.ScheduleRepositoryImpl
import com.TJPK.tapciriz.domain.model.WeekType
import com.TJPK.tapciriz.presentation.schedule.ScheduleScreen
import com.TJPK.tapciriz.presentation.schedule.ScheduleScreenContent
import com.TJPK.tapciriz.presentation.schedule.ScheduleUiState
import com.TJPK.tapciriz.presentation.schedule.ScheduleViewModel
import com.TJPK.tapciriz.ui.theme.TapcirizTheme

/**
 * =========================================================================================
 * СЛОЙ APP / ENTRY POINT: Главная активность приложения.
 * Инициализирует зависимости (SettingsManager, Repository, ViewModel)
 * и запускает декларативный интерфейс Jetpack Compose.
 * =========================================================================================
 */
class MainActivity : ComponentActivity() {

    private val viewModel: ScheduleViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val settingsManager = OneCSettingsManager(applicationContext)
                val repository = ScheduleRepositoryImpl(settingsManager)
                return ScheduleViewModel(repository, settingsManager) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TapcirizTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScheduleScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScheduleScreenPreview() {
    TapcirizTheme {
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

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ScheduleScreenDarkPreview() {
    TapcirizTheme(darkTheme = true) {
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
