package com.androidxcore.habittracker.feature.habits.presentation.stats

sealed interface StatsAction {
    data object OnBackClick : StatsAction
    data object OnRetry : StatsAction
}