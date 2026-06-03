package com.androidxcore.habittracker.core.data.worker

import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.androidxcore.habittracker.core.domain.repository.ReminderScheduler
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReminderSchedulerImpl @Inject constructor(
    private val workManager: WorkManager
) : ReminderScheduler {

    override fun schedule(
        habitId: String,
        habitName: String,
        timeHour: Int,
        timeMinute: Int
    ) {
        val inputData = Data.Builder()
            .putString(HabitReminderWorker.KEY_HABIT_ID, habitId)
            .putString(HabitReminderWorker.KEY_HABIT_NAME, habitName)
            .build()

        val request = PeriodicWorkRequestBuilder<HabitReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInputData(inputData)
            .setInitialDelay(calculateInitialDelay(timeHour, timeMinute), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            habitId,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    override fun cancel(habitId: String) {
        workManager.cancelUniqueWork(habitId)
    }

    override fun cancelAll() {
        workManager.cancelAllWork()
    }

    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }
}