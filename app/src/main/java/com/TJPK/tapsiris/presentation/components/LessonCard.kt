package com.TJPK.tapsiris.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.TJPK.tapsiris.R
import com.TJPK.tapsiris.domain.model.Lesson
import com.TJPK.tapsiris.domain.model.LessonType

/**
 * Чистая карточка учебного занятия в нативном типографическом стиле.
 */
@Composable
fun LessonCard(
    lesson: Lesson,
    canManage: Boolean = false,
    onManageClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isCurrent = lesson.isCurrent
    val hasDelay = (lesson.delayMinutes > 0) || !lesson.delayNotice.isNullOrBlank()

    // Семантический акцентный цвет для типа занятия
    val typeAccentColor = when (lesson.type) {
        LessonType.LECTURE  -> Color(0xFF3B82F6) // Синий
        LessonType.PRACTICE -> Color(0xFF10B981) // Изумрудно-зеленый
        LessonType.LAB      -> Color(0xFFF59E0B) // Янтарный
        LessonType.EXAM     -> Color(0xFFEF4444) // Красный
    }

    val lessonTypeLabel = stringResource(
        when (lesson.type) {
            LessonType.LECTURE  -> R.string.lesson_type_lecture
            LessonType.PRACTICE -> R.string.lesson_type_practice
            LessonType.LAB      -> R.string.lesson_type_lab
            LessonType.EXAM     -> R.string.lesson_type_exam
        }
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isCurrent) 1.5.dp else 1.dp,
            color = if (isCurrent) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // ─── ЛЕВАЯ КОЛОНКА: Номер и время ──────────────────────────────────
            Column(
                modifier = Modifier.width(62.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Номер пары в строгом бейдже
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isCurrent) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "${lesson.number} пара",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Время начала
                Text(
                    text = lesson.timeStart,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp
                )
                // Время окончания
                Text(
                    text = lesson.timeEnd,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }

            // Вертикальная разделительная полоска-акцент
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(typeAccentColor.copy(alpha = if (isCurrent) 1f else 0.85f))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // ─── ПРАВАЯ КОЛОНКА: Предмет, бейджи, препод ────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Заголовок предмета + кнопка управления
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = lesson.subject,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (canManage && onManageClick != null) {
                            IconButton(
                                onClick = onManageClick,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(start = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.teacher_manage_lesson),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Чипсы кабинета, типа и текущей пары
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Кабинет / аудитория
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Text(
                                text = lesson.room,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        // Тип пары
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = typeAccentColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = lessonTypeLabel,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = typeAccentColor,
                                fontSize = 11.sp
                            )
                        }

                        if (isCurrent) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = stringResource(R.string.lesson_current),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Предупреждение о задержке
                    if (hasDelay) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEE2E2), // Светло-красный
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color(0xFFB91C1C),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (lesson.delayMinutes > 0 && !lesson.delayNotice.isNullOrBlank())
                                        stringResource(R.string.teacher_delay_banner, lesson.delayMinutes, lesson.delayNotice)
                                    else if (lesson.delayMinutes > 0)
                                        stringResource(R.string.teacher_delay_banner_short, lesson.delayMinutes)
                                    else
                                        lesson.delayNotice ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Предупреждение о замене
                    if (lesson.replacementInfo != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7), // Светло-янтарный
                            border = BorderStroke(1.dp, Color(0xFFFCD34D))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${stringResource(R.string.lesson_replacement_label)}: ${lesson.replacementInfo}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF92400E),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Преподаватель
                Text(
                    text = lesson.teacher,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
