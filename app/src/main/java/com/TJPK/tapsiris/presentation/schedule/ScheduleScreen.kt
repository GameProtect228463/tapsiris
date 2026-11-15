package com.TJPK.tapsiris.presentation.schedule

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.TJPK.tapsiris.R
import com.TJPK.tapsiris.data.local.UserRole
import com.TJPK.tapsiris.domain.model.Lesson
import com.TJPK.tapsiris.domain.model.WeekType
import com.TJPK.tapsiris.presentation.components.AdminManageDialog
import com.TJPK.tapsiris.presentation.components.BellScheduleDialog
import com.TJPK.tapsiris.presentation.components.DaySelector
import com.TJPK.tapsiris.presentation.components.GroupSelectSheet
import com.TJPK.tapsiris.presentation.components.LessonCard
import com.TJPK.tapsiris.presentation.components.TeacherManageDialog

/**
 * =========================================================================================
 * СЛОЙ PRESENTATION / SCREEN: Главный экран расписания (чистый нативный интерфейс).
 * =========================================================================================
 */
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onOpenSettings: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onEvent(ScheduleUiEvent.DismissMessage)
        }
    }

    ScheduleScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onOpenSettings = onOpenSettings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreenContent(
    state: ScheduleUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (ScheduleUiEvent) -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    var showGroupSheet by remember { mutableStateOf(false) }
    var showBellsDialog by remember { mutableStateOf(false) }
    val groupSheetState = rememberModalBottomSheetState()

    var showAdminDialog by remember { mutableStateOf(false) }
    var adminLessonToEdit by remember { mutableStateOf<Lesson?>(null) }

    var showTeacherDialog by remember { mutableStateOf(false) }
    var teacherLessonToManage by remember { mutableStateOf<Lesson?>(null) }

    // Анимация вращения кнопки обновления
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(800, easing = LinearEasing)),
        label = "angle"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.currentUserRole == UserRole.ADMINISTRATOR) {
                ExtendedFloatingActionButton(
                    onClick = {
                        adminLessonToEdit = null
                        showAdminDialog = true
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.admin_panel_title),
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = if (state.canChangeGroup) {
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showGroupSheet = true }
                                .padding(vertical = 2.dp)
                        } else Modifier
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (state.canChangeGroup) {
                                    state.selectedGroup?.name ?: stringResource(R.string.select_group)
                                } else {
                                    "${stringResource(R.string.my_group)}: ${state.selectedGroup?.name ?: state.assignedGroupName}"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (state.canChangeGroup) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = stringResource(R.string.action_select_group),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Тонкий статус синхронизации 1С
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onOpenSettings() }
                                .padding(vertical = 1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (state.is1CConnected) Color(0xFF10B981)
                                        else Color(0xFFF59E0B)
                                    )
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (state.is1CConnected)
                                    stringResource(R.string.status_1c_online, state.lastUpdatedText)
                                else
                                    stringResource(R.string.status_1c_offline, state.lastUpdatedText),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onEvent(ScheduleUiEvent.ToggleSearch(!state.isSearchActive))
                    }) {
                        Icon(
                            imageVector = if (state.isSearchActive) Icons.Default.Close
                            else Icons.Default.Search,
                            contentDescription = stringResource(R.string.action_search)
                        )
                    }
                    IconButton(onClick = { showBellsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.action_bells)
                        )
                    }
                    IconButton(
                        onClick = { onEvent(ScheduleUiEvent.Refresh) },
                        enabled = !state.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.action_refresh),
                            modifier = Modifier.rotate(if (state.isRefreshing) rotationAngle else 0f)
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.action_settings)
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
        ) {
            // Строка поиска
            AnimatedVisibility(visible = state.isSearchActive) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { onEvent(ScheduleUiEvent.SearchQueryChanged(it)) },
                    placeholder = { Text(stringResource(R.string.search_schedule)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                onEvent(ScheduleUiEvent.SearchQueryChanged(""))
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null)
                            }
                        }
                    }
                )
            }

            // Демо-баннер
            AnimatedVisibility(visible = state.isDemoMode) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.demo_mode_banner),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Сегментированный переключатель недели (Числитель / Знаменатель)
            WeekTypeSegmentedControl(
                selectedWeek = state.weekType,
                onSelectWeek = { onEvent(ScheduleUiEvent.SelectWeekType(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Основной контент: загрузка / ошибка / данные
            AnimatedContent(
                targetState = state.loadState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "content_state"
            ) { loadState ->
                when (loadState) {
                    // ── Загрузка ──────────────────────────────────────────────────
                    is ScheduleLoadState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    strokeWidth = 3.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = loadState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // ── Ошибка подключения ────────────────────────────────────────
                    is ScheduleLoadState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = loadState.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = loadState.hint,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                if (loadState.showSettingsButton) {
                                    Button(onClick = onOpenSettings) {
                                        Text(stringResource(R.string.open_settings))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                OutlinedButton(onClick = { onEvent(ScheduleUiEvent.Refresh) }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.retry))
                                }
                            }
                        }
                    }

                    // ── Данные загружены ──────────────────────────────────────────
                    is ScheduleLoadState.Success -> {
                        ScheduleContentView(
                            state = state,
                            onEvent = onEvent,
                            onLessonManage = { lesson ->
                                if (state.currentUserRole == UserRole.ADMINISTRATOR) {
                                    adminLessonToEdit = lesson
                                    showAdminDialog = true
                                } else if (state.currentUserRole == UserRole.TEACHER) {
                                    teacherLessonToManage = lesson
                                    showTeacherDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showGroupSheet && state.canChangeGroup) {
        GroupSelectSheet(
            groups = state.groups,
            selectedGroup = state.selectedGroup,
            sheetState = groupSheetState,
            onDismiss = { showGroupSheet = false },
            onGroupSelected = { onEvent(ScheduleUiEvent.SelectGroup(it)) }
        )
    }

    if (showBellsDialog) {
        BellScheduleDialog(
            bells = state.bells,
            onDismiss = { showBellsDialog = false }
        )
    }

    if (showAdminDialog) {
        AdminManageDialog(
            selectedGroup = state.selectedGroup,
            lessonToEdit = adminLessonToEdit,
            onDismiss = {
                showAdminDialog = false
                adminLessonToEdit = null
            },
            onCreateGroup = { group ->
                onEvent(ScheduleUiEvent.CreateGroup(group))
                showAdminDialog = false
                adminLessonToEdit = null
            },
            onDeleteGroup = { groupId ->
                onEvent(ScheduleUiEvent.DeleteGroup(groupId))
                showAdminDialog = false
                adminLessonToEdit = null
            },
            onSaveLesson = { lesson ->
                onEvent(ScheduleUiEvent.SaveLesson(lesson))
                showAdminDialog = false
                adminLessonToEdit = null
            },
            onDeleteLesson = { lessonId ->
                onEvent(ScheduleUiEvent.DeleteLesson(lessonId))
                showAdminDialog = false
                adminLessonToEdit = null
            }
        )
    }

    if (showTeacherDialog && teacherLessonToManage != null) {
        TeacherManageDialog(
            lesson = teacherLessonToManage!!,
            onDismiss = {
                showTeacherDialog = false
                teacherLessonToManage = null
            },
            onChangeRoom = { newRoom ->
                onEvent(ScheduleUiEvent.ChangeRoom(teacherLessonToManage!!.id, newRoom))
                showTeacherDialog = false
                teacherLessonToManage = null
            },
            onSetReplacement = { replacementInfo, newTeacher, newSubject ->
                onEvent(ScheduleUiEvent.SetReplacement(teacherLessonToManage!!.id, replacementInfo, newTeacher, newSubject))
                showTeacherDialog = false
                teacherLessonToManage = null
            },
            onNotifyDelay = { delayMinutes, delayNotice ->
                onEvent(ScheduleUiEvent.NotifyDelay(teacherLessonToManage!!.id, delayMinutes, delayNotice))
                showTeacherDialog = false
                teacherLessonToManage = null
            }
        )
    }
}

/**
 * Чистый сегментированный переключатель недели (Числитель / Знаменатель).
 */
@Composable
private fun WeekTypeSegmentedControl(
    selectedWeek: WeekType,
    onSelectWeek: (WeekType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val isOdd = selectedWeek == WeekType.ODD
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelectWeek(WeekType.ODD) },
                shape = RoundedCornerShape(8.dp),
                color = if (isOdd) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (isOdd) 1.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.week_odd),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isOdd) FontWeight.Bold else FontWeight.Medium,
                        color = if (isOdd) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val isEven = selectedWeek == WeekType.EVEN
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelectWeek(WeekType.EVEN) },
                shape = RoundedCornerShape(8.dp),
                color = if (isEven) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (isEven) 1.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.week_even),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isEven) FontWeight.Bold else FontWeight.Medium,
                        color = if (isEven) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** Контент экрана когда данные успешно загружены */
@Composable
private fun ScheduleContentView(
    state: ScheduleUiState,
    onEvent: (ScheduleUiEvent) -> Unit,
    onLessonManage: (Lesson) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Селектор дней
        DaySelector(
            days = state.weekSchedule,
            selectedDayIndex = state.selectedDayIndex,
            onDaySelected = { onEvent(ScheduleUiEvent.SelectDay(it)) }
        )

        // Чистая сводка дня без громоздкой карточки
        val currentDay = state.currentDay
        if (currentDay != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentDay.dayFullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (currentDay.lessons.isEmpty())
                            stringResource(R.string.no_lessons_count)
                        else
                            "${currentDay.lessons.size} пар(ы) • ${currentDay.lessons.first().timeStart} — ${currentDay.lessons.last().timeEnd}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }

                val todayIndex = state.weekSchedule.indexOfFirst { it.isToday }
                if (todayIndex != -1 && state.selectedDayIndex != todayIndex) {
                    TextButton(
                        onClick = { onEvent(ScheduleUiEvent.SelectDay(todayIndex)) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.today),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Список занятий или пустое состояние
        val displayLessons = state.displayLessons
        if (displayLessons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (state.isSearchActive && state.searchQuery.isNotEmpty())
                            stringResource(R.string.search_schedule_empty)
                        else
                            stringResource(R.string.no_lessons_today),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (state.isSearchActive && state.searchQuery.isNotEmpty())
                            stringResource(R.string.search_schedule_empty_hint)
                        else
                            stringResource(R.string.no_lessons_today_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val canManage = state.currentUserRole in listOf(UserRole.TEACHER, UserRole.ADMINISTRATOR)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayLessons, key = { it.id }) { lesson ->
                    LessonCard(
                        lesson = lesson,
                        canManage = canManage,
                        onManageClick = { onLessonManage(lesson) }
                    )
                }
            }
        }
    }
}
