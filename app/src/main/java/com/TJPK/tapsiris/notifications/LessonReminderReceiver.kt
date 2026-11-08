package com.TJPK.tapsiris.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.TJPK.tapsiris.data.local.NotificationSettingsManager

/**
 * Приёмник запланированных системных будильников (AlarmManager)
 * для отправки напоминаний о начале пар.
 */
class LessonReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_LESSON_NAME = "extra_lesson_name"
        const val EXTRA_ROOM = "extra_room"
        const val EXTRA_TEACHER = "extra_teacher"
        const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val settingsManager = NotificationSettingsManager(context)
        if (!settingsManager.isRemindersEnabled) return

        val lessonName = intent.getStringExtra(EXTRA_LESSON_NAME) ?: return
        val room = intent.getStringExtra(EXTRA_ROOM) ?: ""
        val teacher = intent.getStringExtra(EXTRA_TEACHER) ?: ""
        val minutesBefore = intent.getIntExtra(EXTRA_MINUTES_BEFORE, settingsManager.minutesBeforeLesson)

        NotificationHelper.sendLessonReminderNotification(
            context = context,
            lessonName = lessonName,
            room = room,
            teacher = teacher,
            minutesBefore = minutesBefore
        )
    }
}
