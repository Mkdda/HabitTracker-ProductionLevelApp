package com.androidxcore.habittracker.feature.habits.presentation.stats

sealed interface StatsEvent {
    data object NavigateBack : StatsEvent
}