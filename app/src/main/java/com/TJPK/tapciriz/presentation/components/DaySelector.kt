package com.TJPK.tapciriz.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.TJPK.tapciriz.domain.model.DaySchedule

/**
 * =========================================================================================
 * СЛОЙ PRESENTATION / COMPONENT: Горизонтальный селектор дней недели.
 *
 * Обязанности:
 * - Отображение дней учебной недели (Пн, Вт, Ср, Чт, Пт, Сб).
 * - Выделение текущего выбранного дня (плавная анимация смены цветов).
 * - Индикатор наличия замен (оранжевая точка) или обычных пар.
 * - Маркер "Сегодня" под актуальным календарным днем.
 * =========================================================================================
 */
@Composable
fun DaySelector(
    days: List<DaySchedule>,
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(days) { index, day ->
            val isSelected = index == selectedDayIndex
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                label = "bgColor"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "contentColor"
            )

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onDaySelected(index) },
                shape = RoundedCornerShape(16.dp),
                color = bgColor,
                tonalElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .width(58.dp)
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.dayShortName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = contentColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val dateOnly = day.dateText.split(" ").firstOrNull() ?: ""
                    Text(
                        text = dateOnly,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Индикаторы: замена (оранжевая точка) или точка пары
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(6.dp)
                    ) {
                        if (day.hasReplacements) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFFFFD54F) else Color(0xFFFF9800))
                            )
                        } else if (day.lessons.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(contentColor.copy(alpha = 0.5f))
                            )
                        } else {
                            Spacer(modifier = Modifier.size(5.dp))
                        }
                    }

                    if (day.isToday) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Сегодня",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
