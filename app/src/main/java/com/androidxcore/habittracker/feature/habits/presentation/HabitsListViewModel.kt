package com.androidxcore.habittracker.feature.habits.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidxcore.habittracker.R
import com.androidxcore.habittracker.core.domain.usecase.CalculateStreakUseCase
import com.androidxcore.habittracker.core.domain.usecase.CompleteHabitError
import com.androidxcore.habittracker.core.domain.usecase.CompleteHabitUseCase
import com.androidxcore.habittracker.core.domain.usecase.GetHabitsUseCase
import com.androidxcore.habittracker.core.domain.usecase.IsHabitCompletedTodayUseCase
import com.androidxcore.habittracker.core.domain.util.onFailure
import com.androidxcore.habittracker.core.domain.util.onSuccess
import com.androidxcore.habittracker.core.presentation.UiText
import com.androidxcore.habittracker.core.presentation.toUiText
import com.androidxcore.habittracker.feature.habits.presentation.model.toHabitUi
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
class HabitsListViewModel @Inject constructor(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val completeHabitUseCase: CompleteHabitUseCase,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val isCompletedTodayUseCase: IsHabitCompletedTodayUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HabitsListState())
    val state = _state.asStateFlow()

    private val _events = Channel<HabitsListEvent>()
    val events = _events.receiveAsFlow()

    private var observeJob: Job? = null

    init {
        observeHabits()
    }

    private fun observeHabits() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            getHabitsUseCase().collect { habits ->
                val habitsUi = habits.map { habit ->
                    val streak = calculateStreakUseCase(habit.id, habit.createdAt, habit.frequencyPerWeek)
                    val isCompleted = isCompletedTodayUseCase(habit.id)
                    habit.toHabitUi(streak).copy(isCompletedToday = isCompleted)
                }
                _state.update { it.copy(habits = habitsUi, isLoading = false) }
            }
        }
    }

    fun onAction(action: HabitsListAction) {
        when (action) {
            is HabitsListAction.OnCompleteHabit -> completeHabit(action.habitId)
            is HabitsListAction.OnHabitClick -> {
                viewModelScope.launch {
                    _events.send(HabitsListEvent.NavigateToDetail(action.habitId))
                }
            }
            is HabitsListAction.OnAddHabitClick -> {
                viewModelScope.launch {
                    _events.send(HabitsListEvent.NavigateToCreateHabit)
                }
            }
            is HabitsListAction.OnRetry -> observeHabits()
        }
    }

    private fun completeHabit(habitId: String) {
        viewModelScope.launch {
            completeHabitUseCase(habitId)
                .onSuccess {
                    _state.update { state ->
                        state.copy(habits = state.habits.map { habit ->
                            if (habit.id == habitId) habit.copy(isCompletedToday = true) else habit
                        })
                    }
                }
                .onFailure { error ->
                    val message = when (error) {
                        is CompleteHabitError.AlreadyCompletedToday ->
                            UiText.StringResource(R.string.error_already_completed)
                        is CompleteHabitError.Storage ->
                            error.error.toUiText()
                    }
                    _events.send(HabitsListEvent.ShowError(message))
                }
        }
    }
}
