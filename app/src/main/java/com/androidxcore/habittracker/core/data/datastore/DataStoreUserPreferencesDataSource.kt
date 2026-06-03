package com.androidxcore.habittracker.core.data.datastore

import androidx.datastore.core.DataStore
import com.androidxcore.habittracker.core.domain.model.UserPreferences
import com.androidxcore.habittracker.core.domain.repository.UserPreferencesDataSource
import kotlinx.coroutines.flow.Flow

// Not wired to Hilt — UserPreferences feature is not yet used in production.
// Reintroduce @Inject and register in DatabaseModule when a real preference is needed.
class DataStoreUserPreferencesDataSource(
    private val dataStore: DataStore<UserPreferences>
) : UserPreferencesDataSource {

    override fun observe(): Flow<UserPreferences> = dataStore.data

    override suspend fun updateDarkTheme(useDarkTheme: Boolean) {
        dataStore.updateData { it.copy(useDarkTheme = useDarkTheme) }
    }

    override suspend fun updateShowCompleted(show: Boolean) {
        dataStore.updateData { it.copy(showCompletedHabits = show) }
    }

    override suspend fun updateDefaultReminderTime(time: String) {
        dataStore.updateData { it.copy(defaultReminderTime = time) }
    }

    override suspend fun setOnboardingCompleted() {
        dataStore.updateData { it.copy(hasCompletedOnboarding = true) }
    }
}
