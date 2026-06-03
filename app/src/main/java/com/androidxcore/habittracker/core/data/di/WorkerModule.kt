package com.androidxcore.habittracker.core.data.di

import android.content.Context
import androidx.work.WorkManager
import com.androidxcore.habittracker.core.data.worker.ReminderSchedulerImpl
import com.androidxcore.habittracker.core.domain.repository.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulerModule {

    @Binds
    @Singleton
    abstract fun bindReminderScheduler(
        impl: ReminderSchedulerImpl
    ): ReminderScheduler
}