package com.TJPK.tapsiris.presentation.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.TJPK.tapsiris.R
import com.TJPK.tapsiris.data.local.OneCSettingsManager
import com.TJPK.tapsiris.domain.repository.ScheduleRepository
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
 * =========================================================================================
 */
class ScheduleViewModel(
    private val repository: ScheduleRepository,
    private val settingsManager: OneCSettingsManager,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ScheduleUiState(serverConfig = settingsManager.getConfig())
    )
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

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
                        userMessage = appContext.getString(R.string.server_settings_saved)
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
            // Шаг 1: Загрузка групп
            _uiState.update {
                it.copy(loadState = ScheduleLoadState.Loading(
                    appContext.getString(R.string.loading_groups)
                ))
            }

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

            // Шаг 2: Загрузка звонков
            val bellsResult = repository.getBells()
            if (bellsResult.isSuccess) {
                _uiState.update { it.copy(bells = bellsResult.getOrNull().orEmpty()) }
            }

            // Шаг 3: Загрузка расписания
            loadSchedule()
        }
    }

    fun loadSchedule() {
        val config = _uiState.value.serverConfig
        val currentSelectedGroup = _uiState.value.selectedGroup
        val currentWeekType = _uiState.value.weekType

        if (_uiState.value.isRefreshing) return

        // Если адрес сервера не настроен и демо-режим отключён — показываем ошибку сразу
        if (config.serverUrl.isBlank() && !config.isDemoMode) {
            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    loadState = ScheduleLoadState.Error(
                        title = appContext.getString(R.string.error_no_server),
                        hint = appContext.getString(R.string.error_no_server_hint),
                        showSettingsButton = true
                    )
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isRefreshing = true,
                loadState = ScheduleLoadState.Loading(
                    appContext.getString(R.string.loading_schedule)
                )
            )
        }

        viewModelScope.launch {
            val groupId = currentSelectedGroup?.id ?: ""
            val scheduleResult = repository.getSchedule(groupId, currentWeekType)
            val isDemoMode = settingsManager.getConfig().isDemoMode

            _uiState.update { state ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val nowTime = timeFormat.format(Date())

                if (scheduleResult.isSuccess) {
                    val days = scheduleResult.getOrNull().orEmpty()
                    state.copy(
                        weekSchedule = days,
                        isRefreshing = false,
                        isDemoMode = isDemoMode,
                        is1CConnected = !isDemoMode,
                        lastUpdatedText = if (isDemoMode) "Demo"
                        else appContext.getString(R.string.synced_at, nowTime),
                        loadState = ScheduleLoadState.Success,
                        userMessage = if (!isDemoMode)
                            appContext.getString(R.string.synced_success) else null
                    )
                } else {
                    val errorMsg = scheduleResult.exceptionOrNull()?.localizedMessage
                        ?: appContext.getString(R.string.error_connection)
                    state.copy(
                        isRefreshing = false,
                        isDemoMode = false,
                        is1CConnected = false,
                        lastUpdatedText = appContext.getString(R.string.status_offline),
                        loadState = ScheduleLoadState.Error(
                            title = appContext.getString(R.string.error_connection),
                            hint = appContext.getString(R.string.error_connection_hint),
                            showSettingsButton = false
                        ),
                        userMessage = appContext.getString(R.string.sync_error, errorMsg)
                    )
                }
            }
        }
    }
}
