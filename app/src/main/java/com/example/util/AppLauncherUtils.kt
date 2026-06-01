package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.widget.Toast

object AppLauncherUtils {

    fun launchGoogleCalendar(context: Context) {
        val packageName = "com.google.android.calendar"
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        } else {
            // Fallback to web link
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://calendar.google.com"))
            context.startActivity(webIntent)
            Toast.makeText(context, "Mở Lịch trên trình duyệt", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchGoogleMeet(context: Context) {
        val packageName = "com.google.android.apps.tachyon" // Google Meet (Duo transition)
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        } else {
            // Fallback to meet web link
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://meet.google.com"))
            context.startActivity(webIntent)
            Toast.makeText(context, "Mở Google Meet trên trình duyệt", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchGoogleDrive(context: Context) {
        val packageName = "com.google.android.apps.docs"
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        } else {
            // Fallback to drive web link
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com"))
            context.startActivity(webIntent)
            Toast.makeText(context, "Mở Google Drive trên trình duyệt", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchClockAlarm(context: Context) {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Try standard deskclock launcher package names
            val packages = listOf(
                "com.google.android.deskclock",
                "com.android.deskclock",
                "com.sec.android.app.clockpackage",
                "com.huawei.deskclock",
                "com.lenovo.deskclock"
            )
            var launched = false
            for (p in packages) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(p)
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                    launched = true
                    break
                }
            }
            if (!launched) {
                Toast.makeText(context, "Không thể mở ứng dụng đồng hồ của thiết bị", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchZalo(context: Context) {
        val packageName = "com.zing.zalo"
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://zalo.me"))
            context.startActivity(webIntent)
            Toast.makeText(context, "Mở Zalo trên trình duyệt", Toast.LENGTH_SHORT).show()
        }
    }
}
