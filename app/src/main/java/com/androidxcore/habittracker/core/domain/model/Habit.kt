package com.androidxcore.habittracker.core.domain.model

data class Habit(
    val id: String,
    val name: String,
    val emoji: String,
    val frequencyPerWeek: Int,
    val reminderTime: String?,
    val createdAt: Long
)
