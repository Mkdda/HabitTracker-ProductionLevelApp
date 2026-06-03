package com.androidxcore.habittracker.feature.habits.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidxcore.habittracker.core.domain.usecase.CalculateStreakUseCase
import com.androidxcore.habittracker.core.domain.usecase.GetHabitsUseCase
import com.androidxcore.habittracker.core.domain.usecase.GetWeeklyCompletionsUseCase
import com.androidxcore.habittracker.core.domain.usecase.IsHabitCompletedTodayUseCase
import com.androidxcore.habittracker.feature.habits.presentation.model.OverallStatsUi
import com.androidxcore.habittracker.feature.habits.presentation.model.toHabitStatsUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val isCompletedTodayUseCase: IsHabitCompletedTodayUseCase,
    private val getWeeklyCompletionsUseCase: GetWeeklyCompletionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(StatsState())
    val state = _state.asStateFlow()

    private val _events = Channel<StatsEvent>()
    val events = _events.receiveAsFlow()

    private var statsJob: Job? = null

    init {
        observeStats()
    }

    fun onAction(action: StatsAction) {
        when (action) {
            is StatsAction.OnBackClick -> {
                viewModelScope.launch { _events.send(StatsEvent.NavigateBack) }
            }
            is StatsAction.OnRetry -> observeStats()
        }
    }

    private fun observeStats() {
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            getHabitsUseCase().collect { habits ->
                if (habits.isEmpty()) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            habits = emptyList(),
                            overallStats = OverallStatsUi(0, 0, 0f, "", 0, "days")
                        )
                    }
                    return@collect
                }

                val habitsUi = habits.map { habit ->
                    val streak = calculateStreakUseCase(habit.id, habit.createdAt, habit.frequencyPerWeek)
                    val weekly = getWeeklyCompletionsUseCase(habit.id)
                    habit.toHabitStatsUi(streak, weekly)
                }

                val completedToday = habits.count { habit ->
                    isCompletedTodayUseCase.invoke(habit.id)
                }

                val bestHabit = habitsUi.maxByOrNull { it.currentStreak }

                val overallStats = OverallStatsUi(
                    totalHabits = habits.size,
                    completedToday = completedToday,
                    averageCompletionRate = habitsUi
                        .map { it.completionRate }
                        .average()
                        .toFloat(),
                    bestStreakHabitName = bestHabit?.name ?: "",
                    bestStreak = bestHabit?.currentStreak ?: 0,
                    bestStreakUnit = bestHabit?.streakUnit ?: "days"
                )

                _state.update {
                    it.copy(
                        isLoading = false,
                        habits = habitsUi,
                        overallStats = overallStats
                    )
                }
            }
        }
    }
}
