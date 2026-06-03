package com.androidxcore.habittracker.feature.habits.presentation.model

import com.androidxcore.habittracker.core.domain.model.Habit
import com.androidxcore.habittracker.core.domain.usecase.StreakResult

fun Habit.toHabitDetailUi(
    streak: StreakResult,
    isCompletedToday: Boolean
): HabitDetailUi = HabitDetailUi(
    id = id,
    name = name,
    emoji = emoji,
    frequencyLabel = frequencyPerWeek.toFrequencyLabel(),
    currentStreak = streak.currentStreak,
    longestStreak = streak.longestStreak,
    streakUnit = streak.streakUnit,
    completionRate = streak.completionRate,
    isCompletedToday = isCompletedToday,
    reminderTime = reminderTime
)
