package com.androidxcore.habittracker.feature.habits.presentation.model

import com.androidxcore.habittracker.core.domain.model.Habit
import com.androidxcore.habittracker.core.domain.usecase.StreakResult

fun Habit.toHabitStatsUi(
    streak: StreakResult,
    completionsLast7Days: List<Boolean>
): HabitStatsUi = HabitStatsUi(
    id = id,
    name = name,
    emoji = emoji,
    currentStreak = streak.currentStreak,
    streakUnit = streak.streakUnit,
    completionRate = streak.completionRate,
    completionsLast7Days = completionsLast7Days
)
