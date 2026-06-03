package com.androidxcore.habittracker.feature.habits.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.androidxcore.habittracker.R
import com.androidxcore.habittracker.core.domain.usecase.CalculateStreakUseCase
import com.androidxcore.habittracker.core.domain.usecase.CompleteHabitError
import com.androidxcore.habittracker.core.domain.usecase.CompleteHabitUseCase
import com.androidxcore.habittracker.core.domain.usecase.DeleteHabitUseCase
import com.androidxcore.habittracker.core.domain.usecase.GetHabitByIdUseCase
import com.androidxcore.habittracker.core.domain.usecase.IsHabitCompletedTodayUseCase
import com.androidxcore.habittracker.core.domain.util.Result
import com.androidxcore.habittracker.core.presentation.UiText
import com.androidxcore.habittracker.core.presentation.toUiText
import com.androidxcore.habittracker.feature.habits.presentation.model.toHabitDetailUi
import com.androidxcore.habittracker.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getHabitByIdUseCase: GetHabitByIdUseCase,
    private val completeHabitUseCase: CompleteHabitUseCase,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val isCompletedTodayUseCase: IsHabitCompletedTodayUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase
) : ViewModel() {

    private val habitId = savedStateHandle.toRoute<Screen.HabitDetail>().habitId

    private val _state = MutableStateFlow(HabitDetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<HabitDetailEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadHabit()
    }

    fun onAction(action: HabitDetailAction) {
        when (action) {
            is HabitDetailAction.OnBackClick -> {
                viewModelScope.launch { _events.send(HabitDetailEvent.NavigateBack) }
            }
            is HabitDetailAction.OnCompleteClick -> completeHabit()
            is HabitDetailAction.OnDeleteClick -> _state.update { it.copy(showDeleteDialog = true) }
            is HabitDetailAction.OnConfirmDelete -> deleteHabit()
            is HabitDetailAction.OnDismissDelete -> _state.update { it.copy(showDeleteDialog = false) }
            is HabitDetailAction.OnRetry -> loadHabit()
        }
    }

    private fun loadHabit() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = getHabitByIdUseCase(habitId)) {
                is Result.Success -> {
                    val habit = result.data
                    val streak = calculateStreakUseCase(habit.id, habit.createdAt, habit.frequencyPerWeek)
                    val isCompleted = isCompletedTodayUseCase(habit.id)
                    _state.update {
                        it.copy(
                            habit = habit.toHabitDetailUi(streak, isCompleted),
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = UiText.StringResource(R.string.error_not_found)
                        )
                    }
                }
            }
        }
    }

    private fun completeHabit() {
        viewModelScope.launch {
            _state.update { it.copy(isCompleting = true) }
            when (val result = completeHabitUseCase(habitId)) {
                is Result.Success -> loadHabit()
                is Result.Error -> {
                    _state.update { it.copy(isCompleting = false) }
                    val message = when (result.error) {
                        is CompleteHabitError.AlreadyCompletedToday ->
                            UiText.StringResource(R.string.error_already_completed)
                        is CompleteHabitError.Storage ->
                            result.error.error.toUiText()
                    }
                    _events.send(HabitDetailEvent.ShowError(message))
                }
            }
        }
    }

    private fun deleteHabit() {
        viewModelScope.launch {
            _state.update { it.copy(showDeleteDialog = false) }
            when (deleteHabitUseCase(habitId)) {
                is Result.Success -> _events.send(HabitDetailEvent.NavigateBack)
                is Result.Error -> _events.send(
                    HabitDetailEvent.ShowError(
                        UiText.StringResource(R.string.error_unknown)
                    )
                )
            }
        }
    }
}
