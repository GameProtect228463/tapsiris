package com.TJPK.tapsiris.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.TJPK.tapsiris.R
import com.TJPK.tapsiris.domain.model.Lesson
import com.TJPK.tapsiris.domain.model.LessonType
import com.TJPK.tapsiris.domain.model.StudentGroup
import java.util.UUID

/**
 * =========================================================================================
 * СЛОЙ PRESENTATION / DIALOG: Диалог управления для Администратора (1С Синхронизация).
 * Включает:
 * 1. Создание новых учебных групп в 1С.
 * 2. Добавление и изменение учебных занятий (пар) с указанием кабинетов.
 * 3. Удаление занятий или групп.
 * =========================================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageDialog(
    selectedGroup: StudentGroup?,
    lessonToEdit: Lesson? = null,
    onDismiss: () -> Unit,
    onCreateGroup: (StudentGroup) -> Unit,
    onDeleteGroup: (groupId: String) -> Unit,
    onSaveLesson: (Lesson) -> Unit,
    onDeleteLesson: (lessonId: String) -> Unit
) {
    // Если передан lessonToEdit — сразу открываем вкладку редактирования пары (таб 1)
    var selectedTab by remember { mutableIntStateOf(if (lessonToEdit != null) 1 else 0) }

    // ─── Поля группы ─────────────────────────────────────────────────────────
    var groupName by remember { mutableStateOf("") }
    var groupCourseText by remember { mutableStateOf("1") }
    var groupDepartment by remember { mutableStateOf("Информационные технологии") }

    // ─── Поля пары ───────────────────────────────────────────────────────────
    var lessonNumberText by remember { mutableStateOf(lessonToEdit?.number?.toString() ?: "1") }
    var lessonTimeStart by remember { mutableStateOf(lessonToEdit?.timeStart ?: "08:30") }
    var lessonTimeEnd by remember { mutableStateOf(lessonToEdit?.timeEnd ?: "10:00") }
    var lessonSubject by remember { mutableStateOf(lessonToEdit?.subject ?: "") }
    var lessonTeacher by remember { mutableStateOf(lessonToEdit?.teacher ?: "") }
    var lessonRoom by remember { mutableStateOf(lessonToEdit?.room ?: "") }
    var lessonType by remember { mutableStateOf(lessonToEdit?.type ?: LessonType.PRACTICE) }
    var lessonTypeDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.admin_panel_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.admin_create_group), fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                if (lessonToEdit != null) stringResource(R.string.admin_edit_lesson)
                                else stringResource(R.string.admin_add_lesson),
                                fontSize = 11.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
                        // ─── Создание группы ──────────────────────────────────
                        OutlinedTextField(
                            value = groupName,
                            onValueChange = { groupName = it },
                            label = { Text(stringResource(R.string.admin_group_name)) },
                            placeholder = { Text("ИС-21, П-22") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Group, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = groupCourseText,
                            onValueChange = { groupCourseText = it.filter { ch -> ch.isDigit() } },
                            label = { Text(stringResource(R.string.admin_course)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = groupDepartment,
                            onValueChange = { groupDepartment = it },
                            label = { Text(stringResource(R.string.admin_department)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (groupName.isNotBlank()) {
                                    val newGroup = StudentGroup(
                                        id = UUID.randomUUID().toString(),
                                        name = groupName.trim(),
                                        course = groupCourseText.toIntOrNull() ?: 1,
                                        department = groupDepartment.trim().ifBlank { "Колледж" }
                                    )
                                    onCreateGroup(newGroup)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.save_and_sync_1c))
                        }

                        // Удаление выбранной группы (если есть)
                        if (selectedGroup != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    onDeleteGroup(selectedGroup.id)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.admin_delete_group) + ": ${selectedGroup.name}")
                            }
                        }
                    }

                    1 -> {
                        // ─── Добавление / редактирование пары ─────────────────
                        OutlinedTextField(
                            value = lessonSubject,
                            onValueChange = { lessonSubject = it },
                            label = { Text(stringResource(R.string.admin_lesson_subject)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = lessonTeacher,
                            onValueChange = { lessonTeacher = it },
                            label = { Text(stringResource(R.string.admin_lesson_teacher)) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = lessonRoom,
                            onValueChange = { lessonRoom = it },
                            label = { Text(stringResource(R.string.admin_lesson_room)) },
                            placeholder = { Text("каб. 308, ауд. 102") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = lessonNumberText,
                                onValueChange = { lessonNumberText = it.filter { ch -> ch.isDigit() } },
                                label = { Text(stringResource(R.string.admin_lesson_number)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = lessonTimeStart,
                                onValueChange = { lessonTimeStart = it },
                                label = { Text(stringResource(R.string.admin_lesson_time_start)) },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null)
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Тип занятия (Dropdown)
                        ExposedDropdownMenuBox(
                            expanded = lessonTypeDropdownExpanded,
                            onExpandedChange = { lessonTypeDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = lessonType.title,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.admin_lesson_type)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lessonTypeDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = lessonTypeDropdownExpanded,
                                onDismissRequest = { lessonTypeDropdownExpanded = false }
                            ) {
                                LessonType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.title) },
                                        onClick = {
                                            lessonType = type
                                            lessonTypeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (lessonSubject.isNotBlank()) {
                                    val num = lessonNumberText.toIntOrNull() ?: 1
                                    val savedLesson = Lesson(
                                        id = lessonToEdit?.id ?: "les_${UUID.randomUUID()}",
                                        number = num,
                                        timeStart = lessonTimeStart.trim().ifBlank { "08:30" },
                                        timeEnd = lessonTimeEnd.trim().ifBlank { "10:00" },
                                        subject = lessonSubject.trim(),
                                        teacher = lessonTeacher.trim().ifBlank { "Преподаватель" },
                                        room = lessonRoom.trim().ifBlank { "каб. -" },
                                        type = lessonType,
                                        isCurrent = lessonToEdit?.isCurrent ?: false,
                                        isCanceled = lessonToEdit?.isCanceled ?: false,
                                        replacementInfo = lessonToEdit?.replacementInfo,
                                        delayNotice = lessonToEdit?.delayNotice,
                                        delayMinutes = lessonToEdit?.delayMinutes ?: 0
                                    )
                                    onSaveLesson(savedLesson)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.save_and_sync_1c))
                        }

                        // Кнопка удаления пары (если редактируем существующую)
                        if (lessonToEdit != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    onDeleteLesson(lessonToEdit.id)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.admin_delete_lesson))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
