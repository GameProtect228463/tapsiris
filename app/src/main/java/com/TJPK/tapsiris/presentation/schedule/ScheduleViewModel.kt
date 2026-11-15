package com.TJPK.tapsiris.presentation.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.TJPK.tapsiris.R
import com.TJPK.tapsiris.data.local.NotificationSettingsManager
import com.TJPK.tapsiris.data.local.OneCSettingsManager
import com.TJPK.tapsiris.data.local.UserManager
import com.TJPK.tapsiris.data.local.UserProfile
import com.TJPK.tapsiris.data.local.UserRole
import com.TJPK.tapsiris.domain.model.StudentGroup
import com.TJPK.tapsiris.domain.repository.ScheduleRepository
import com.TJPK.tapsiris.notifications.NotificationHelper
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
 * Загружает группы из базы данных и разграничивает доступ:
 * Студент видит только свою группу (без возможности переключения),
 * Преподаватели и Администраторы могут просматривать любые группы.
 * =========================================================================================
 */
class ScheduleViewModel(
    private val repository: ScheduleRepository,
    private val settingsManager: OneCSettingsManager,
    private val userManager: UserManager,
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
                if (!_uiState.value.canChangeGroup) return
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
                viewModelScope.launch {
                    // Автоматически подгружаем свежие группы из 1С базы и обновляем локальную БД SQLite
                    val groupsResult = repository.getGroups()
                    val allGroups = groupsResult.getOrNull().orEmpty()
                    applyGroupsForProfile(allGroups, userManager.getUserProfile())
                    loadSchedule()
                }
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
            is ScheduleUiEvent.UpdateUserContext -> {
                applyUserProfile(event.profile)
            }
            ScheduleUiEvent.DismissMessage -> {
                _uiState.update { it.copy(userMessage = null) }
            }

            // ─── Обработка действий Администратора ────────────────────────────
            is ScheduleUiEvent.CreateGroup -> {
                viewModelScope.launch {
                    val result = repository.createGroup(event.group)
                    if (result.isSuccess) {
                        _uiState.update { it.copy(userMessage = appContext.getString(R.string.admin_group_created)) }
                        val freshGroups = repository.getGroups().getOrNull().orEmpty()
                        applyGroupsForProfile(freshGroups, userManager.getUserProfile())
                        loadSchedule()
                    } else {
                        _uiState.update { it.copy(userMessage = result.exceptionOrNull()?.localizedMessage) }
                    }
                }
            }
            is ScheduleUiEvent.DeleteGroup -> {
                viewModelScope.launch {
                    val result = repository.deleteGroup(event.groupId)
                    if (result.isSuccess) {
                        _uiState.update { it.copy(userMessage = appContext.getString(R.string.admin_group_deleted)) }
                        val freshGroups = repository.getGroups().getOrNull().orEmpty()
                        applyGroupsForProfile(freshGroups, userManager.getUserProfile())
                        loadSchedule()
                    } else {
                        _uiState.update { it.copy(userMessage = result.exceptionOrNull()?.localizedMessage) }
                    }
                }
            }
            is ScheduleUiEvent.SaveLesson -> {
                val currentGroup = _uiState.value.selectedGroup ?: return
                val weekType = _uiState.value.weekType
                val dayIndex = _uiState.value.selectedDayIndex + 1
                viewModelScope.launch {
                    val result = repository.saveLesson(currentGroup.id, weekType, dayIndex, event.lesson)
                    if (result.isSuccess) {
                        _uiState.update { it.copy(userMessage = appContext.getString(R.string.admin_action_saved)) }
                        loadSchedule()
                    } else {
                        _uiState.update { it.copy(userMessage = result.exceptionOrNull()?.localizedMessage) }
                    }
                }
            }
            is ScheduleUiEvent.DeleteLesson -> {
                val currentGroup = _uiState.value.selectedGroup ?: return
                val weekType = _uiState.value.weekType
                val dayIndex = _uiState.value.selectedDayIndex + 1
                viewModelScope.launch {
                    val result = repository.deleteLesson(currentGroup.id, weekType, dayIndex, event.lessonId)
                    if (result.isSuccess) {
                        _uiState.update { it.copy(userMessage = appContext.getString(R.string.admin_lesson_deleted)) }
                        loadSchedule()
                    } else {
                        _uiState.update { it.copy(userMessage = result.exceptionOrNull()?.localizedMessage) }
                    }
                }
            }

            // ─── Обработка действий Преподавателя ─────────────────────────────
            is ScheduleUiEvent.ChangeRoom -> {
                val currentGroup = _uiState.value.selectedGroup ?: return
                val weekType = _uiState.value.weekType
                val dayIndex = _uiState.value.selectedDayIndex + 1
                viewModelScope.launch {
                    val result = repository.changeRoom(currentGroup.id, weekType, dayIndex, event.lessonId, event.newRoom)
                    if (result.isSuccess) {
                        _uiState.update { it.copy(userMessage = appContext.getString(R.string.admin_action_saved)) }
                        loadSchedule()
                    } else {
                        _uiState.update { it.copy(userMessage = result.exceptionOrNull()?.localizedMessage) }
                    }
                }
            }
            is ScheduleUiEvent.SetReplacement -> {
                val currentGroup = _uiState.value.selectedGroup ?: return
                val weekType = _uiState.value.weekType
                val dayIndex = _uiState.value.selectedDayIndex + 1
                viewModelScope.launch {
                    val result = repository.setReplacement(
                        currentGroup.id, weekType, dayIndex, event.lessonId,
                        event.replacementInfo, event.newTeacher, event.newSubject
                    )
                    if (result.isSuccess) {
                        _uiState.update { it.copy(userMessage = appContext.getString(R.string.admin_action_saved)) }
                        loadSchedule()
                    } else {
                        _uiState.update { it.copy(userMessage = result.exceptionOrNull()?.localizedMessage) }
                    }
                }
            }
            is ScheduleUiEvent.NotifyDelay -> {
                val currentGroup = _uiState.value.selectedGroup ?: return
                val weekType = _uiState.value.weekType
                val dayIndex = _uiState.value.selectedDayIndex + 1
                viewModelScope.launch {
                    val result = repository.notifyDelay(
                        currentGroup.id, weekType, dayIndex, event.lessonId,
                        event.delayMinutes, event.delayNotice
                    )
                    if (result.isSuccess) {
                        _uiState.update { it.copy(userMessage = appContext.getString(R.string.admin_action_saved)) }
                        loadSchedule()
                    } else {
                        _uiState.update { it.copy(userMessage = result.exceptionOrNull()?.localizedMessage) }
                    }
                }
            }
        }
    }

    private fun loadInitialData() {
        val userProfile = userManager.getUserProfile()
        viewModelScope.launch {
            // Шаг 1: Загрузка групп из 1С (или локальной БД при офлайн)
            _uiState.update {
                it.copy(loadState = ScheduleLoadState.Loading(
                    appContext.getString(R.string.loading_groups)
                ))
            }

            val groupsResult = repository.getGroups()
            val allGroups = groupsResult.getOrNull().orEmpty()

            if (groupsResult.isFailure) {
                val err = groupsResult.exceptionOrNull()?.localizedMessage
                if (!err.isNullOrBlank()) {
                    _uiState.update { it.copy(userMessage = err) }
                }
            }

            applyGroupsForProfile(allGroups, userProfile)

            // Шаг 2: Загрузка звонков
            val bellsResult = repository.getBells()
            if (bellsResult.isSuccess) {
                _uiState.update { it.copy(bells = bellsResult.getOrNull().orEmpty()) }
            }

            // Шаг 3: Загрузка расписания
            loadSchedule()
        }
    }

    private fun applyUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            val groupsResult = repository.getGroups()
            val allGroups = groupsResult.getOrNull().orEmpty()
            applyGroupsForProfile(allGroups, profile)
            loadSchedule()
        }
    }

    private fun applyGroupsForProfile(allGroups: List<StudentGroup>, profile: UserProfile) {
        val isStudent = profile.role == UserRole.STUDENT && !profile.isGuest

        if (isStudent && profile.groupName.isNotBlank()) {
            // Зарегистрированный студент видит расписание своей группы
            val studentGroup = when {
                profile.groupId.isNotBlank() ->
                    allGroups.find { it.id == profile.groupId }
                        ?: allGroups.find { it.name.equals(profile.groupName, ignoreCase = true) }
                profile.groupName.isNotBlank() ->
                    allGroups.find { it.name.equals(profile.groupName, ignoreCase = true) }
                else -> allGroups.firstOrNull()
            } ?: StudentGroup(
                id = profile.groupId.ifBlank { profile.groupName },
                name = profile.groupName.ifBlank { allGroups.firstOrNull()?.name ?: "Группа" },
                course = 1,
                department = profile.department.ifBlank { "Студенческое отделение" }
            )

            _uiState.update {
                it.copy(
                    groups = if (allGroups.isNotEmpty()) allGroups else listOf(studentGroup),
                    selectedGroup = studentGroup,
                    canChangeGroup = false,
                    currentUserRole = UserRole.STUDENT,
                    assignedGroupName = studentGroup.name
                )
            }
        } else {
            // Гость, Преподаватель или Администратор — видят все группы из базы 1С
            _uiState.update {
                it.copy(
                    groups = allGroups,
                    selectedGroup = when {
                        profile.groupId.isNotBlank() ->
                            allGroups.find { g -> g.id == profile.groupId }
                                ?: allGroups.find { g -> g.name.equals(profile.groupName, ignoreCase = true) }
                                ?: it.selectedGroup ?: allGroups.firstOrNull()
                        profile.groupName.isNotBlank() ->
                            allGroups.find { g -> g.name.equals(profile.groupName, ignoreCase = true) }
                                ?: it.selectedGroup ?: allGroups.firstOrNull()
                        else -> it.selectedGroup ?: allGroups.firstOrNull()
                    },
                    canChangeGroup = true,
                    currentUserRole = profile.role,
                    assignedGroupName = profile.groupName
                )
            }
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
            // Используем id группы из 1С (UUID) для точного запроса расписания
            val groupId = currentSelectedGroup?.id ?: ""
            val scheduleResult = repository.getSchedule(groupId, currentWeekType)
            val isDemoMode = settingsManager.getConfig().isDemoMode

            _uiState.update { state ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val nowTime = timeFormat.format(Date())

                if (scheduleResult.isSuccess) {
                    val days = scheduleResult.getOrNull().orEmpty()

                    // Проверка уведомлений о заменах
                    val notificationSettings = NotificationSettingsManager(appContext)
                    if (notificationSettings.isReplacementsEnabled && days.any { it.hasReplacements }) {
                        NotificationHelper.sendReplacementNotification(appContext)
                    }

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
