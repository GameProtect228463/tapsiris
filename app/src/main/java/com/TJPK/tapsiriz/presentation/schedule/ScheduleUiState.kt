package com.TJPK.tapsiriz.presentation.schedule

import com.TJPK.tapsiriz.data.remote.ServerConfig
import com.TJPK.tapsiriz.domain.model.BellTime
import com.TJPK.tapsiriz.domain.model.DaySchedule
import com.TJPK.tapsiriz.domain.model.StudentGroup
import com.TJPK.tapsiriz.domain.model.WeekType

/**
 * =========================================================================================
 * СЛОЙ PRESENTATION: Состояние экрана расписания (UI State).
 * Единственный неизменяемый (immutable) источник правды для отрисовки Compose UI.
 * =========================================================================================
 *
 * @property groups Доступные для выбора группы студентов
 * @property selectedGroup Выбранная в данный момент группа
 * @property weekType Текущая учебная неделя: Числитель (ODD) или Знаменатель (EVEN)
 * @property selectedDayIndex Индекс выбранного дня недели (0..5 для Пн..Сб)
 * @property weekSchedule Расписание на всю текущую неделю
 * @property bells Расписание звонков и перемен колледжа
 * @property serverConfig Настройки подключения к серверу 1С
 * @property isRefreshing Идет ли процесс сетевой синхронизации
 * @property is1CConnected Доступен ли сервер 1С (true = онлайн, false = оффлайн/кэш)
 * @property lastUpdatedText Текстовый статус времени последнего обновления
 * @property isSearchActive Открыта ли строка поиска
 * @property searchQuery Текущий поисковый запрос (по предмету, аудитории или преподавателю)
 * @property userMessage Всплывающее информационное сообщение для Snackbar
 */
data class ScheduleUiState(
    val groups: List<StudentGroup> = emptyList(),
    val selectedGroup: StudentGroup? = null,
    val weekType: WeekType = WeekType.ODD,
    val selectedDayIndex: Int = 2, // По умолчанию Среда (2)
    val weekSchedule: List<DaySchedule> = emptyList(),
    val bells: List<BellTime> = emptyList(),
    val serverConfig: ServerConfig = ServerConfig(),
    val isRefreshing: Boolean = false,
    val is1CConnected: Boolean = false,
    val lastUpdatedText: String = "Загрузка...",
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val userMessage: String? = null
) {
    /** Выбранный день недели или null */
    val currentDay: DaySchedule?
        get() = weekSchedule.getOrNull(selectedDayIndex) ?: weekSchedule.firstOrNull()

    /** Отфильтрованный список уроков выбранного дня с учетом поискового запроса */
    val displayLessons: List<com.TJPK.tapsiriz.domain.model.Lesson>
        get() {
            val lessons = currentDay?.lessons ?: emptyList()
            if (!isSearchActive || searchQuery.isBlank()) {
                return lessons
            }
            return lessons.filter {
                it.subject.contains(searchQuery, ignoreCase = true) ||
                        it.teacher.contains(searchQuery, ignoreCase = true) ||
                        it.room.contains(searchQuery, ignoreCase = true)
            }
        }
}

/**
 * Действия пользователя на экране (UI Events / Intents в паттерне MVI).
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
