package com.androidxcore.habittracker.core.domain.repository

interface ReminderScheduler {
    fun schedule(habitId: String, habitName: String, timeHour: Int, timeMinute: Int)
    fun cancel(habitId: String)
    fun cancelAll()
}