package com.androidxcore.habittracker.feature.habits.presentation.model

data class HabitUi(
    val id: String,
    val name: String,
    val emoji: String,
    val currentStreak: Int,
    val streakUnit: String,
    val isCompletedToday: Boolean,
    val frequencyLabel: String
)
