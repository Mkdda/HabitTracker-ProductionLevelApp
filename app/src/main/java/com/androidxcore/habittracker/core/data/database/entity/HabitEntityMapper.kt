package com.androidxcore.habittracker.core.data.database.entity

import com.androidxcore.habittracker.core.domain.model.Habit

fun HabitEntity.toHabit(): Habit = Habit(
    id = id,
    name = name,
    emoji = emoji,
    frequencyPerWeek = frequencyPerWeek,
    reminderTime = reminderTime,
    createdAt = createdAt
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    name = name,
    emoji = emoji,
    frequencyPerWeek = frequencyPerWeek,
    reminderTime = reminderTime,
    createdAt = createdAt
)