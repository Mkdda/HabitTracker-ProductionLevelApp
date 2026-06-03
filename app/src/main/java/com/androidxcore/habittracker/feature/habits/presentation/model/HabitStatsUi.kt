package com.androidxcore.habittracker.feature.habits.presentation.model

data class HabitStatsUi(
    val id: String,
    val name: String,
    val emoji: String,
    val currentStreak: Int,
    val streakUnit: String,
    val completionRate: Float,
    val completionsLast7Days: List<Boolean>
)

data class OverallStatsUi(
    val totalHabits: Int,
    val completedToday: Int,
    val averageCompletionRate: Float,
    val bestStreakHabitName: String,
    val bestStreak: Int,
    val bestStreakUnit: String
)
