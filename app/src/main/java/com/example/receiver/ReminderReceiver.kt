package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("TASK_ID", -1)
        val content = intent.getStringExtra("TASK_CONTENT") ?: "Lịch trình của bạn"
        val departureTime = intent.getStringExtra("TASK_DEP_TIME") ?: ""
        val arrivalTime = intent.getStringExtra("TASK_ARR_TIME") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "time_management_reminders"

        // Create Channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nhắc nhở lịch trình",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh gửi thông báo nhắc nhở 15 phút trước giờ xuất phát."
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Action when clicking the notification
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = "Sắp đến giờ xuất phát ($departureTime) - Phải có mặt lúc $arrivalTime. Kiểm tra ngay checklist chuẩn bị!"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Chuẩn bị xuất phát: $content")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskId, notification)
    }
}
