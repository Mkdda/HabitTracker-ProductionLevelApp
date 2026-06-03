package com.androidxcore.habittracker.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val emoji: String,
    val frequencyPerWeek: Int,   // 7 = daily, 3 = 3x per week
    val reminderTime: String?,   // "08:00" nullable — puede no tener reminder
    val createdAt: Long          // epoch millis
)