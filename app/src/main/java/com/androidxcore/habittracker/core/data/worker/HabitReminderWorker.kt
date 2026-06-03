package com.androidxcore.habittracker.core.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.androidxcore.habittracker.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_HABIT_ID = "habit_id"
        const val KEY_HABIT_NAME = "habit_name"
        const val CHANNEL_ID = "habit_reminders"
    }

    override suspend fun doWork(): Result {
        val habitId = inputData.getString(KEY_HABIT_ID) ?: return Result.failure()
        val habitName = inputData.getString(KEY_HABIT_NAME) ?: return Result.failure()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        showNotification(habitId, habitName)

        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Habit Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily reminders for your habits"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun showNotification(habitId: String, habitName: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time for your habit!")
            .setContentText("Don't forget: $habitName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(habitId.hashCode(), notification)
    }
}
