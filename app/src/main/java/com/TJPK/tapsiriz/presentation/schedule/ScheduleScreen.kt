package com.TJPK.tapsiriz.presentation.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.TJPK.tapsiriz.domain.model.WeekType
import com.TJPK.tapsiriz.presentation.components.BellScheduleDialog
import com.TJPK.tapsiriz.presentation.components.DaySelector
import com.TJPK.tapsiriz.presentation.components.GroupSelectSheet
import com.TJPK.tapsiriz.presentation.components.LessonCard

/**
 * =========================================================================================
 * СЛОЙ PRESENTATION / SCREEN: Главный экран расписания.
 *
 * Принимает [ScheduleViewModel] и подписывается на его [ScheduleUiState].
 * Все действия пользователя передаются через метод [ScheduleViewModel.onEvent].
 * =========================================================================================
 */
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onOpenSettings: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Показ всплывающих уведомлений из ViewModel
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

/**
 * Презентационный компонент экрана (Stateless composable).
 * Удобен для изолированного тестирования и Compose Preview.
 */
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
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing)
        ),
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
                                text = state.selectedGroup?.name ?: "Выберите группу",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Выбрать группу",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(ScheduleUiEvent.ToggleSearch(!state.isSearchActive)) }) {
                        Icon(
                            imageVector = if (state.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Поиск по расписанию"
                        )
                    }

                    IconButton(onClick = { showBellsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Расписание звонков"
                        )
                    }

                    // Кнопка настроек приложения
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки"
                        )
                    }

                    IconButton(
                        onClick = { onEvent(ScheduleUiEvent.Refresh) },
                        enabled = !state.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Синхронизация с 1С",
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
            AnimatedVisibility(visible = state.isSearchActive) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { onEvent(ScheduleUiEvent.SearchQueryChanged(it)) },
                    placeholder = { Text("Поиск предмета, кабинета, учителя...") },
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
                            IconButton(onClick = { onEvent(ScheduleUiEvent.SearchQueryChanged("")) }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null)
                            }
                        }
                    }
                )
            }

            // Статус подключения и переключатель недели
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
                            .background(if (state.is1CConnected) Color(0xFF4CAF50) else Color(0xFFFFA000))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.is1CConnected) "1С онлайн • ${state.lastUpdatedText}"
                        else "1С: ${state.lastUpdatedText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = state.weekType == WeekType.ODD,
                        onClick = { onEvent(ScheduleUiEvent.SelectWeekType(WeekType.ODD)) },
                        label = { Text("Числитель", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    FilterChip(
                        selected = state.weekType == WeekType.EVEN,
                        onClick = { onEvent(ScheduleUiEvent.SelectWeekType(WeekType.EVEN)) },
                        label = { Text("Знаменатель", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Селектор дней недели
            DaySelector(
                days = state.weekSchedule,
                selectedDayIndex = state.selectedDayIndex,
                onDaySelected = { onEvent(ScheduleUiEvent.SelectDay(it)) }
            )

            // Сводка выбранного дня
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
                                text = if (currentDay.lessons.isEmpty()) "Пар нет"
                                else "${currentDay.lessons.size} пары • 08:30 — ${currentDay.lessons.last().timeEnd}",
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
                                        text = "Сегодня",
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

            // Список занятий
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
                            text = if (state.isSearchActive && state.searchQuery.isNotEmpty()) "Ничего не найдено"
                            else "В этот день пар нет!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.isSearchActive && state.searchQuery.isNotEmpty()) "Попробуйте изменить поисковый запрос"
                            else "Отличный день для отдыха или самоподготовки 🎉",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayLessons, key = { it.id }) { lesson ->
                        LessonCard(lesson = lesson)
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
