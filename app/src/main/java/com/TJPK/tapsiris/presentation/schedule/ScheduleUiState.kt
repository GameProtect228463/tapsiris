package com.TJPK.tapsiris.presentation.schedule

import com.TJPK.tapsiris.data.remote.ServerConfig
import com.TJPK.tapsiris.domain.model.BellTime
import com.TJPK.tapsiris.domain.model.DaySchedule
import com.TJPK.tapsiris.domain.model.Lesson
import com.TJPK.tapsiris.domain.model.StudentGroup
import com.TJPK.tapsiris.domain.model.WeekType

/**
 * Состояния загрузки данных с 1С-сервера.
 */
sealed interface ScheduleLoadState {
    /** Данные загружаются — показываем прогресс-индикатор */
    data class Loading(val message: String = "") : ScheduleLoadState

    /** Ошибка получения данных — показываем экран ошибки */
    data class Error(
        val title: String,
        val hint: String,
        val showSettingsButton: Boolean = false
    ) : ScheduleLoadState

    /** Данные успешно получены (онлайн или из кэша) */
    object Success : ScheduleLoadState
}

/**
 * =========================================================================================
 * СЛОЙ PRESENTATION: Состояние экрана расписания (UI State).
 * =========================================================================================
 */
data class ScheduleUiState(
    val groups: List<StudentGroup> = emptyList(),
    val selectedGroup: StudentGroup? = null,
    val weekType: WeekType = WeekType.ODD,
    val selectedDayIndex: Int = 2,
    val weekSchedule: List<DaySchedule> = emptyList(),
    val bells: List<BellTime> = emptyList(),
    val serverConfig: ServerConfig = ServerConfig(),
    val isRefreshing: Boolean = false,
    val is1CConnected: Boolean = false,
    val isDemoMode: Boolean = false,
    val lastUpdatedText: String = "",
    val loadState: ScheduleLoadState = ScheduleLoadState.Loading(),
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val userMessage: String? = null
) {
    val currentDay: DaySchedule?
        get() = weekSchedule.getOrNull(selectedDayIndex) ?: weekSchedule.firstOrNull()

    val displayLessons: List<Lesson>
        get() {
            val lessons = currentDay?.lessons ?: emptyList()
            if (!isSearchActive || searchQuery.isBlank()) return lessons
            return lessons.filter {
                it.subject.contains(searchQuery, ignoreCase = true) ||
                        it.teacher.contains(searchQuery, ignoreCase = true) ||
                        it.room.contains(searchQuery, ignoreCase = true)
            }
        }
}

/**
 * Действия пользователя на экране (MVI Events).
 */
sealed interface ScheduleUiEvent {
    data class SelectGroup(val group: StudentGroup) : ScheduleUiEvent
    data class SelectWeekType(val weekType: WeekType) : ScheduleUiEvent
    data class SelectDay(val dayIndex: Int) : ScheduleUiEvent
    data class SearchQueryChanged(val query: String) : ScheduleUiEvent
    data class ToggleSearch(val active: Boolean) : ScheduleUiEvent
    object Refresh : ScheduleUiEvent
    data class UpdateServerConfig(val newConfig: ServerConfig) : ScheduleUiEvent
    object DismissMessage : ScheduleUiEvent
}
