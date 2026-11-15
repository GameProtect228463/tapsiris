package com.TJPK.tapsiris.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import com.TJPK.tapsiris.domain.model.DaySchedule

/**
 * Минималистичный и чистый селектор дней недели в нативном стиле.
 */
@Composable
fun DaySelector(
    days: List<DaySchedule>,
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (days.size <= 6) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            days.forEachIndexed { index, day ->
                DayItem(
                    day = day,
                    isSelected = index == selectedDayIndex,
                    onClick = { onDaySelected(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(days) { index, day ->
                DayItem(
                    day = day,
                    isSelected = index == selectedDayIndex,
                    onClick = { onDaySelected(index) },
                    modifier = Modifier.width(52.dp)
                )
            }
        }
    }
}

@Composable
private fun DayItem(
    day: DaySchedule,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = day.isToday
    val primaryColor = MaterialTheme.colorScheme.primary

    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> primaryColor
            else       -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        label = "dayBg"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.onPrimary
            isToday    -> primaryColor
            else       -> MaterialTheme.colorScheme.onSurface
        },
        label = "dayContent"
    )

    val border = when {
        !isSelected && isToday -> BorderStroke(1.5.dp, primaryColor)
        else                   -> null
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = border
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // День недели (ПН, ВТ...)
            Text(
                text = day.dayShortName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                color = contentColor.copy(alpha = if (isSelected || isToday) 1f else 0.7f),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Число (из dateText или номер дня)
            val dayNumber = extractDayNumber(day.dateText, day.dayIndex)
            Text(
                text = dayNumber,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.SemiBold,
                color = contentColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Минималистичная точка-индикатор
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            day.hasReplacements     -> if (isSelected) Color(0xFFFFE082) else Color(0xFFFF9800)
                            day.lessons.isNotEmpty() -> if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            else                    -> Color.Transparent
                        }
                    )
            )
        }
    }
}

private fun extractDayNumber(dateText: String, fallbackIndex: Int): String {
    val digits = dateText.takeWhile { it.isDigit() }
    return if (digits.isNotBlank()) digits else fallbackIndex.toString()
}
