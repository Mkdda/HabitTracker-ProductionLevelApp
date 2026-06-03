package com.androidxcore.habittracker.core.domain.usecase

import com.androidxcore.habittracker.core.domain.repository.HabitRepository
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class StreakResult(
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRate: Float,
    val streakUnit: String
)

class CalculateStreakUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habitId: String, createdAt: Long, frequencyPerWeek: Int): StreakResult {
        val now = System.currentTimeMillis()
        val entries = repository.getEntriesInRange(habitId, createdAt, now)
        val unit = if (frequencyPerWeek == 7) "days" else "weeks"

        if (entries.isEmpty()) return StreakResult(0, 0, 0f, unit)

        val completedDays = entries
            .map { normalizeToStartOfDay(it) }
            .toSortedSet()
            .toList()
            .sortedDescending()

        return if (frequencyPerWeek == 7) {
            val currentStreak = calculateCurrentDailyStreak(completedDays)
            val longestStreak = calculateLongestDailyStreak(completedDays)
            val totalDays = daysBetween(createdAt, now).coerceAtLeast(1)
            val completionRate = completedDays.size.toFloat() / totalDays
            StreakResult(currentStreak, longestStreak, completionRate, "days")
        } else {
            calculateWeeklyStreakResult(completedDays, frequencyPerWeek, createdAt, now)
        }
    }

    private fun calculateWeeklyStreakResult(
        completedDays: List<Long>,
        frequencyPerWeek: Int,
        createdAt: Long,
        now: Long
    ): StreakResult {
        val today = normalizeToStartOfDay(now)

        val weekCompletions = mutableMapOf<Int, Int>()
        for (day in completedDays) {
            val daysAgo = ((today - day) / TimeUnit.DAYS.toMillis(1)).toInt().coerceAtLeast(0)
            val weekIndex = daysAgo / 7
            weekCompletions[weekIndex] = (weekCompletions[weekIndex] ?: 0) + 1
        }

        var currentStreak = 0
        var w = 0
        while ((weekCompletions[w] ?: 0) >= frequencyPerWeek) {
            currentStreak++
            w++
        }

        val maxWeek = weekCompletions.keys.maxOrNull() ?: 0
        var longestStreak = 0
        var runningStreak = 0
        for (week in 0..maxWeek) {
            if ((weekCompletions[week] ?: 0) >= frequencyPerWeek) {
                runningStreak++
                if (runningStreak > longestStreak) longestStreak = runningStreak
            } else {
                runningStreak = 0
            }
        }

        val totalWeeks = ((daysBetween(createdAt, now) / 7) + 1).coerceAtLeast(1)
        val completedWeeks = weekCompletions.values.count { it >= frequencyPerWeek }
        val completionRate = completedWeeks.toFloat() / totalWeeks

        return StreakResult(currentStreak, longestStreak, completionRate, "weeks")
    }

    private fun calculateCurrentDailyStreak(sortedDaysDesc: List<Long>): Int {
        if (sortedDaysDesc.isEmpty()) return 0
        val today = normalizeToStartOfDay(System.currentTimeMillis())
        val yesterday = today - TimeUnit.DAYS.toMillis(1)
        if (sortedDaysDesc.first() != today && sortedDaysDesc.first() != yesterday) return 0
        var streak = 1
        for (i in 0 until sortedDaysDesc.size - 1) {
            if (sortedDaysDesc[i] - sortedDaysDesc[i + 1] == TimeUnit.DAYS.toMillis(1)) streak++
            else break
        }
        return streak
    }

    private fun calculateLongestDailyStreak(sortedDaysDesc: List<Long>): Int {
        if (sortedDaysDesc.isEmpty()) return 0
        var longest = 1
        var current = 1
        for (i in 0 until sortedDaysDesc.size - 1) {
            if (sortedDaysDesc[i] - sortedDaysDesc[i + 1] == TimeUnit.DAYS.toMillis(1)) {
                current++
                if (current > longest) longest = current
            } else {
                current = 1
            }
        }
        return longest
    }

    private fun normalizeToStartOfDay(timestamp: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun daysBetween(from: Long, to: Long): Int =
        TimeUnit.MILLISECONDS.toDays(to - from).toInt()
}
