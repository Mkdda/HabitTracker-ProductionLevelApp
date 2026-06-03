package com.androidxcore.habittracker.core.data.di

import android.content.Context
import androidx.room.Room
import com.androidxcore.habittracker.core.data.database.HabitTrackerDatabase
import com.androidxcore.habittracker.core.data.repository.RoomHabitDataSource
import com.androidxcore.habittracker.core.domain.repository.HabitRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HabitTrackerDatabase =
        Room.databaseBuilder(
            context,
            HabitTrackerDatabase::class.java,
            "habitflow.db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideHabitDao(db: HabitTrackerDatabase) = db.habitDao()

    @Provides
    fun provideHabitEntryDao(db: HabitTrackerDatabase) = db.habitEntryDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindHabitRepository(
        impl: RoomHabitDataSource
    ): HabitRepository
}
