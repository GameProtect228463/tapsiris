package com.TJPK.tapciriz.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.TJPK.tapciriz.data.local.OneCSettingsManager
import com.TJPK.tapciriz.domain.model.StudentGroup
import com.TJPK.tapciriz.domain.model.WeekType
import com.TJPK.tapciriz.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * =========================================================================================
 * СЛОЙ PRESENTATION: ViewModel управления экраном расписания.
 *
 * Отвечает за:
 * 1. Хранение и изменение состояния [ScheduleUiState] при конфигурационных изменениях (поворот экрана).
 * 2. Вызов методов репозитория [ScheduleRepository] для получения групп, расписания и звонков.
 * 3. Обработку событий пользователя [ScheduleUiEvent] (UDF - Unidirectional Data Flow).
 * =========================================================================================
 */
class ScheduleViewModel(
    private val repository: ScheduleRepository,
    private val settingsManager: OneCSettingsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ScheduleUiState(serverConfig = settingsManager.getConfig())
    )
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    /**
     * Точка входа для всех пользовательских действий на экране.
     */
    fun onEvent(event: ScheduleUiEvent) {
        when (event) {
            is ScheduleUiEvent.SelectGroup -> {
                _uiState.update { it.copy(selectedGroup = event.group) }
                loadSchedule()
            }
            is ScheduleUiEvent.SelectWeekType -> {
                _uiState.update { it.copy(weekType = event.weekType) }
                loadSchedule()
            }
            is ScheduleUiEvent.SelectDay -> {
                _uiState.update { it.copy(selectedDayIndex = event.dayIndex) }
            }
            is ScheduleUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is ScheduleUiEvent.ToggleSearch -> {
                _uiState.update {
                    it.copy(
                        isSearchActive = event.active,
                        searchQuery = if (!event.active) "" else it.searchQuery
                    )
                }
            }
            ScheduleUiEvent.Refresh -> {
                loadSchedule()
            }
            is ScheduleUiEvent.UpdateServerConfig -> {
                settingsManager.saveConfig(event.newConfig)
                _uiState.update {
                    it.copy(
                        serverConfig = event.newConfig,
                        userMessage = "Настройки сервера 1С сохранены"
                    )
                }
                loadSchedule()
            }
            ScheduleUiEvent.DismissMessage -> {
                _uiState.update { it.copy(userMessage = null) }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // 1. Загрузка групп
            val groupsResult = repository.getGroups()
            if (groupsResult.isSuccess) {
                val groups = groupsResult.getOrNull().orEmpty()
                _uiState.update {
                    it.copy(
                        groups = groups,
                        selectedGroup = it.selectedGroup ?: groups.firstOrNull()
                    )
                }
            }

            // 2. Загрузка звонков
            val bellsResult = repository.getBells()
            if (bellsResult.isSuccess) {
                _uiState.update { it.copy(bells = bellsResult.getOrNull().orEmpty()) }
            }

            // 3. Загрузка расписания
            loadSchedule()
        }
    }

    fun loadSchedule() {
        val currentSelectedGroup = _uiState.value.selectedGroup
        val currentWeekType = _uiState.value.weekType

        if (_uiState.value.isRefreshing) return

        _uiState.update { it.copy(isRefreshing = true) }

        viewModelScope.launch {
            val groupId = currentSelectedGroup?.id ?: ""
            val scheduleResult = repository.getSchedule(groupId, currentWeekType)

            _uiState.update { state ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val nowTime = timeFormat.format(Date())

                if (scheduleResult.isSuccess) {
                    val days = scheduleResult.getOrNull().orEmpty()
                    state.copy(
                        weekSchedule = days,
                        isRefreshing = false,
                        is1CConnected = true,
                        lastUpdatedText = "Сегодня в $nowTime",
                        userMessage = "Расписание синхронизировано с 1С"
                    )
                } else {
                    val errorMsg = scheduleResult.exceptionOrNull()?.localizedMessage ?: "Сбой соединения с 1С"
                    state.copy(
                        isRefreshing = false,
                        is1CConnected = false,
                        lastUpdatedText = "Офлайн (1С недоступен)",
                        userMessage = "1C: $errorMsg (показан офлайн кэш)"
                    )
                }
            }
        }
    }
}
