package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.data.TaskEntity
import com.example.receiver.ReminderReceiver
import java.text.SimpleDateFormat
import java.util.*

object SchedulerUtils {

    // Safely parse date and time strings into timestamp milliseconds
    fun getEpochMillis(dateStr: String, timeStr: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return try {
            val dateVal = sdf.parse("$dateStr $timeStr")
            dateVal?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    // Generate list of future dates based on recurrence type (including original instance)
    fun getFutureDates(startDateStr: String, recurringType: String): List<String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dates = mutableListOf<String>()
        val calendar = Calendar.getInstance()

        try {
            val dateVal = sdf.parse(startDateStr) ?: return emptyList()
            calendar.time = dateVal
        } catch (e: Exception) {
            return emptyList()
        }

        val countLimit = when (recurringType) {
            "DAILY" -> 30
            "WEEKLY" -> 12
            "MONTHLY" -> 12
            "YEARLY" -> 5
            else -> 0
        }

        for (i in 1..countLimit) {
            when (recurringType) {
                "DAILY" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
                "YEARLY" -> calendar.add(Calendar.YEAR, 1)
            }
            dates.add(sdf.format(calendar.time))
        }
        return dates
    }

    // Schedule standard or exact alarm for local notification
    fun scheduleTaskReminder(context: Context, task: TaskEntity) {
        val departureEpoch = getEpochMillis(task.date, task.departureTime)
        if (departureEpoch <= 0L) return

        val now = System.currentTimeMillis()
        val reminderBufferMillis = 15 * 60 * 1000 // 15 Minutes

        var targetReminderEpoch = departureEpoch - reminderBufferMillis

        // If target 15-minute-buffer warning has passed but departureTime has not arrived yet,
        // we trigger the notification after 5 seconds of delay.
        if (targetReminderEpoch < now) {
            if (departureEpoch > now) {
                targetReminderEpoch = now + 5000 // 5 seconds from now
            } else {
                Log.d("SchedulerUtils", "Task is in the past, skipping schedule.")
                return // Task in past, skip
            }
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_CONTENT", task.content)
            putExtra("TASK_DEP_TIME", task.departureTime)
            putExtra("TASK_ARR_TIME", task.arrivalTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel previous if any
        try {
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            Log.e("SchedulerUtils", "Error canceling alarm", e)
        }

        // Check if we can schedule exact alarms (Android 12+ / Target SDK 31+)
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        try {
            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetReminderEpoch,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetReminderEpoch,
                    pendingIntent
                )
            }
            Log.d("SchedulerUtils", "Alarm scheduled for Task ${task.id} at ${Date(targetReminderEpoch)}")
        } catch (e: SecurityException) {
            // Fallback to inexact alarm if security policy restricts exact alarms
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                targetReminderEpoch,
                pendingIntent
            )
            Log.d("SchedulerUtils", "Fallback inexact alarm scheduled for Task ${task.id}")
        }
    }

    // Cancel dynamic task reminder
    fun cancelTaskReminder(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("SchedulerUtils", "Canceled alarm for Task $taskId")
        }
    }
}
