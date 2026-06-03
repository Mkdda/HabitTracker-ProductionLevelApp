package com.androidxcore.habittracker.feature.habits.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidxcore.habittracker.R
import com.androidxcore.habittracker.core.domain.error.HabitError
import com.androidxcore.habittracker.core.domain.usecase.CreateHabitError
import com.androidxcore.habittracker.core.domain.usecase.CreateHabitUseCase
import com.androidxcore.habittracker.core.domain.util.Result
import com.androidxcore.habittracker.core.presentation.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateHabitViewModel @Inject constructor(
    private val createHabitUseCase: CreateHabitUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateHabitState())
    val state = _state.asStateFlow()

    private val _events = Channel<CreateHabitEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: CreateHabitAction) {
        when (action) {
            is CreateHabitAction.OnNameChange -> {
                _state.update { it.copy(name = action.name, nameError = null) }
            }
            is CreateHabitAction.OnEmojiChange -> {
                _state.update { it.copy(emoji = action.emoji) }
            }
            is CreateHabitAction.OnFrequencyChange -> {
                _state.update { it.copy(frequencyPerWeek = action.frequency) }
            }
            is CreateHabitAction.OnReminderToggle -> {
                _state.update {
                    it.copy(
                        isReminderEnabled = !it.isReminderEnabled,
                        reminderTime = if (!it.isReminderEnabled) "08:00" else null
                    )
                }
            }
            is CreateHabitAction.OnReminderTimeChange -> {
                _state.update { it.copy(reminderTime = action.time) }
            }
            is CreateHabitAction.OnSaveClick -> saveHabit()
            is CreateHabitAction.OnBackClick -> {
                viewModelScope.launch { _events.send(CreateHabitEvent.NavigateBack) }
            }
        }
    }

    private fun saveHabit() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val current = _state.value

            when (val result = createHabitUseCase(
                name = current.name,
                emoji = current.emoji,
                frequencyPerWeek = current.frequencyPerWeek,
                reminderTime = current.reminderTime
            )) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(CreateHabitEvent.HabitCreated)
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    when (result.error) {
                        is CreateHabitError.Validation -> {
                            val uiText = when (result.error.error) {
                                HabitError.EMPTY_NAME ->
                                    UiText.StringResource(R.string.error_empty_name)
                                HabitError.NAME_TOO_LONG ->
                                    UiText.StringResource(R.string.error_name_too_long)
                                HabitError.INVALID_FREQUENCY ->
                                    UiText.StringResource(R.string.error_invalid_frequency)
                            }
                            _state.update { it.copy(nameError = uiText) }
                        }
                        is CreateHabitError.Storage -> {
                            _events.send(CreateHabitEvent.NavigateBack)
                        }
                    }
                }
            }
        }
    }
}
