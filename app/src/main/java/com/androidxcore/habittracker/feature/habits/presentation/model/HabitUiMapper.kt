package com.androidxcore.habittracker.feature.habits.presentation.model

import com.androidxcore.habittracker.core.domain.model.Habit
import com.androidxcore.habittracker.core.domain.usecase.StreakResult

fun Habit.toHabitUi(streak: StreakResult): HabitUi = HabitUi(
    id = id,
    name = name,
    emoji = emoji,
    currentStreak = streak.currentStreak,
    streakUnit = streak.streakUnit,
    isCompletedToday = false,
    frequencyLabel = frequencyPerWeek.toFrequencyLabel()
)
