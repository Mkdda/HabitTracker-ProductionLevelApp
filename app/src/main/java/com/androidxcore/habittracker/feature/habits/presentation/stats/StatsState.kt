package com.androidxcore.habittracker.feature.habits.presentation.stats

import com.androidxcore.habittracker.core.presentation.UiText
import com.androidxcore.habittracker.feature.habits.presentation.model.HabitStatsUi
import com.androidxcore.habittracker.feature.habits.presentation.model.OverallStatsUi

data class StatsState(
    val overallStats: OverallStatsUi? = null,
    val habits: List<HabitStatsUi> = emptyList(),
    val isLoading: Boolean = true,
    val error: UiText? = null
)