package com.androidxcore.habittracker.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.androidxcore.habittracker.core.data.database.dao.HabitDao
import com.androidxcore.habittracker.core.data.database.dao.HabitEntryDao
import com.androidxcore.habittracker.core.data.database.entity.HabitEntity
import com.androidxcore.habittracker.core.data.database.entity.HabitEntryEntity

@Database(
    entities = [HabitEntity::class, HabitEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HabitTrackerDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitEntryDao(): HabitEntryDao
}