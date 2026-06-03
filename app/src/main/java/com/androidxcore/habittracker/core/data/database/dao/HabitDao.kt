package com.androidxcore.habittracker.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androidxcore.habittracker.core.data.database.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: String): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: String)
}
