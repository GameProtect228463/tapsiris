package com.TJPK.tapsiris.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
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

/**
 * =========================================================================================
 * СЛОЙ PRESENTATION / DIALOG: Диалог управления парой для Преподавателя (1С Синхронизация).
 * Включает:
 * 1. Смену кабинета / аудитории.
 * 2. Установку замены преподавателя / предмета.
 * 3. Оповещение о задержке на пару.
 * =========================================================================================
 */
@Composable
fun TeacherManageDialog(
    lesson: Lesson,
    onDismiss: () -> Unit,
    onChangeRoom: (newRoom: String) -> Unit,
    onSetReplacement: (replacementInfo: String, newTeacher: String?, newSubject: String?) -> Unit,
    onNotifyDelay: (delayMinutes: Int, delayNotice: String) -> Unit
) {
    // 0 = Кабинет, 1 = Замена, 2 = Задержка
    var selectedTab by remember { mutableIntStateOf(0) }

    // Поля кабинета
    var roomText by remember { mutableStateOf(lesson.room) }

    // Поля замены
    var replacementInfoText by remember { mutableStateOf(lesson.replacementInfo ?: "") }
    var substituteTeacherText by remember { mutableStateOf("") }
    var substituteSubjectText by remember { mutableStateOf("") }

    // Поля задержки
    var delayMinutesText by remember { mutableStateOf(if (lesson.delayMinutes > 0) lesson.delayMinutes.toString() else "10") }
    var delayNoticeText by remember { mutableStateOf(lesson.delayNotice ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.teacher_manage_lesson),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${lesson.number} пара • ${lesson.subject}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                        text = { Text(stringResource(R.string.teacher_change_room), fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.teacher_set_replacement), fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(stringResource(R.string.teacher_notify_delay), fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
                        // ─── Смена кабинета ───────────────────────────────────
                        OutlinedTextField(
                            value = roomText,
                            onValueChange = { roomText = it },
                            label = { Text(stringResource(R.string.admin_lesson_room)) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (roomText.isNotBlank()) {
                                    onChangeRoom(roomText.trim())
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.save_and_sync_1c))
                        }
                    }

                    1 -> {
                        // ─── Замена пары ──────────────────────────────────────
                        OutlinedTextField(
                            value = replacementInfoText,
                            onValueChange = { replacementInfoText = it },
                            label = { Text(stringResource(R.string.teacher_replacement_info)) },
                            placeholder = { Text("Замена преподавателя / перенос") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = substituteTeacherText,
                            onValueChange = { substituteTeacherText = it },
                            label = { Text(stringResource(R.string.teacher_replacement_substitute)) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = substituteSubjectText,
                            onValueChange = { substituteSubjectText = it },
                            label = { Text(stringResource(R.string.teacher_replacement_subject)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                onSetReplacement(
                                    replacementInfoText.trim(),
                                    substituteTeacherText.trim().ifBlank { null },
                                    substituteSubjectText.trim().ifBlank { null }
                                )
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.save_and_sync_1c))
                        }
                    }

                    2 -> {
                        // ─── Предупреждение о задержке ────────────────────────
                        OutlinedTextField(
                            value = delayMinutesText,
                            onValueChange = { delayMinutesText = it.filter { ch -> ch.isDigit() } },
                            label = { Text(stringResource(R.string.teacher_delay_minutes)) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = delayNoticeText,
                            onValueChange = { delayNoticeText = it },
                            label = { Text(stringResource(R.string.teacher_delay_comment)) },
                            placeholder = { Text("Совещание у директора / дорога") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val mins = delayMinutesText.toIntOrNull() ?: 10
                                onNotifyDelay(mins, delayNoticeText.trim())
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.save_and_sync_1c))
                        }

                        if (lesson.delayMinutes > 0 || !lesson.delayNotice.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    onNotifyDelay(0, "")
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text(stringResource(R.string.teacher_clear_delay))
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
