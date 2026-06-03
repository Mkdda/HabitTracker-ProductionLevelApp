package com.androidxcore.habittracker.core.domain.repository

import com.androidxcore.habittracker.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesDataSource {
    fun observe(): Flow<UserPreferences>
    suspend fun updateDarkTheme(useDarkTheme: Boolean)
    suspend fun updateShowCompleted(show: Boolean)
    suspend fun updateDefaultReminderTime(time: String)
    suspend fun setOnboardingCompleted()
}