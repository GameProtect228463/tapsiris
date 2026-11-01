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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.TJPK.tapsiris.domain.model.WeekType
import com.TJPK.tapsiris.presentation.components.BellScheduleDialog
import com.TJPK.tapsiris.presentation.components.DaySelector
import com.TJPK.tapsiris.presentation.components.GroupSelectSheet
import com.TJPK.tapsiris.presentation.components.LessonCard

/**
 * =========================================================================================
 * СЛОЙ PRESENTATION / SCREEN: Главный экран расписания.
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
        topBar = {
            TopAppBar(
                title = {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showGroupSheet = true },
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = state.selectedGroup?.name
                                    ?: stringResource(R.string.select_group),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = stringResource(R.string.action_select_group),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
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
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.action_settings)
                        )
                    }
                    IconButton(
                        onClick = { onEvent(ScheduleUiEvent.Refresh) },
                        enabled = !state.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.action_refresh),
                            modifier = Modifier.rotate(if (state.isRefreshing) rotationAngle else 0f),
                            tint = if (state.isRefreshing) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
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
                    shape = RoundedCornerShape(14.dp),
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

            // Статус 1С + переключатель недели
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenSettings() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.is1CConnected) Color(0xFF4CAF50)
                                else Color(0xFFFFA000)
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.is1CConnected)
                            stringResource(R.string.status_1c_online, state.lastUpdatedText)
                        else
                            stringResource(R.string.status_1c_offline, state.lastUpdatedText),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = state.weekType == WeekType.ODD,
                        onClick = { onEvent(ScheduleUiEvent.SelectWeekType(WeekType.ODD)) },
                        label = { Text(stringResource(R.string.week_odd), fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    FilterChip(
                        selected = state.weekType == WeekType.EVEN,
                        onClick = { onEvent(ScheduleUiEvent.SelectWeekType(WeekType.EVEN)) },
                        label = { Text(stringResource(R.string.week_even), fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

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
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
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
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = loadState.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = loadState.hint,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
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
                                        modifier = Modifier.size(18.dp)
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
                            onOpenSettings = onOpenSettings
                        )
                    }
                }
            }
        }
    }

    if (showGroupSheet) {
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
}

/** Контент экрана когда данные успешно загружены */
@Composable
private fun ScheduleContentView(
    state: ScheduleUiState,
    onEvent: (ScheduleUiEvent) -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Селектор дней
        DaySelector(
            days = state.weekSchedule,
            selectedDayIndex = state.selectedDayIndex,
            onDaySelected = { onEvent(ScheduleUiEvent.SelectDay(it)) }
        )

        // Сводка дня
        val currentDay = state.currentDay
        if (currentDay != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${currentDay.dayFullName}, ${currentDay.dateText}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentDay.lessons.isEmpty())
                                stringResource(R.string.no_lessons_count)
                            else
                                stringResource(
                                    R.string.lessons_count,
                                    currentDay.lessons.size,
                                    currentDay.lessons.last().timeEnd
                                ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val todayIndex = state.weekSchedule.indexOfFirst { it.isToday }
                    if (todayIndex != -1 && state.selectedDayIndex != todayIndex) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onEvent(ScheduleUiEvent.SelectDay(todayIndex)) },
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Today,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.today),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
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
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (state.isSearchActive && state.searchQuery.isNotEmpty())
                            stringResource(R.string.search_schedule_empty)
                        else
                            stringResource(R.string.no_lessons_today),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayLessons, key = { it.id }) { lesson ->
                    LessonCard(lesson = lesson)
                }
            }
        }
    }
}
