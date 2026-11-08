package com.TJPK.tapsiris.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.TJPK.tapsiris.MainActivity
import com.TJPK.tapsiris.R

/**
 * Утилитарный модуль для создания каналов уведомлений, проверки разрешений
 * и отправки системных уведомлений (тестовые, напоминания о парах, замены).
 */
object NotificationHelper {

    const val CHANNEL_SCHEDULE_ID = "channel_schedule_notifications"
    private const val NOTIFICATION_TEST_ID = 1001
    private const val NOTIFICATION_LESSON_ID = 1002
    private const val NOTIFICATION_REPLACEMENT_ID = 1003

    /**
     * Создает канал уведомлений для расписания на Android 8.0 (API 26) и выше.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.getString(R.string.notification_channel_name)
            val channelDescription = context.getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_SCHEDULE_ID, channelName, importance).apply {
                description = channelDescription
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Проверяет, предоставлено ли разрешение на показ уведомлений.
     * Для Android 13+ (API 33+) проверяет runtime-разрешение POST_NOTIFICATIONS.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Отправляет тестовое уведомление для проверки работоспособности.
     * Возвращает true, если уведомление успешно отправлено.
     */
    fun sendTestNotification(context: Context): Boolean {
        createNotificationChannels(context)
        if (!hasNotificationPermission(context)) return false

        val contentIntent = createContentIntent(context)
        val title = context.getString(R.string.notification_test_title)
        val body = context.getString(R.string.notification_test_body)

        val builder = NotificationCompat.Builder(context, CHANNEL_SCHEDULE_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)

        return try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_TEST_ID, builder.build())
            true
        } catch (_: SecurityException) {
            false
        }
    }

    /**
     * Отправляет напоминание о скором начале учебного занятия (пары).
     */
    fun sendLessonReminderNotification(
        context: Context,
        lessonName: String,
        room: String,
        teacher: String,
        minutesBefore: Int = 15
    ): Boolean {
        createNotificationChannels(context)
        if (!hasNotificationPermission(context)) return false

        val contentIntent = createContentIntent(context)
        val title = context.getString(R.string.notification_lesson_reminder_title, lessonName)
        val body = context.getString(R.string.notification_lesson_reminder_body, minutesBefore, room, teacher)

        val builder = NotificationCompat.Builder(context, CHANNEL_SCHEDULE_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)

        return try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_LESSON_ID, builder.build())
            true
        } catch (_: SecurityException) {
            false
        }
    }

    /**
     * Отправляет оповещение о наличии замен в расписании группы.
     */
    fun sendReplacementNotification(
        context: Context,
        details: String = ""
    ): Boolean {
        createNotificationChannels(context)
        if (!hasNotificationPermission(context)) return false

        val contentIntent = createContentIntent(context)
        val title = context.getString(R.string.notification_replacement_title)
        val body = if (details.isNotBlank()) details else context.getString(R.string.notification_replacement_body)

        val builder = NotificationCompat.Builder(context, CHANNEL_SCHEDULE_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)

        return try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_REPLACEMENT_ID, builder.build())
            true
        } catch (_: SecurityException) {
            false
        }
    }

    private fun createContentIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, 0, intent, flags)
    }
}
