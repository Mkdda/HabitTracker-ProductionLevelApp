package com.androidxcore.habittracker.viewmodel

import app.cash.turbine.test
import com.androidxcore.habittracker.core.domain.repository.ReminderScheduler
import com.androidxcore.habittracker.core.domain.usecase.CreateHabitUseCase
import com.androidxcore.habittracker.fake.FakeHabitLocalDataSource
import com.androidxcore.habittracker.feature.habits.presentation.create.CreateHabitAction
import com.androidxcore.habittracker.feature.habits.presentation.create.CreateHabitEvent
import com.androidxcore.habittracker.feature.habits.presentation.create.CreateHabitViewModel
import com.androidxcore.habittracker.util.MainDispatcherRule
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CreateHabitViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dataSource: FakeHabitLocalDataSource
    private lateinit var reminderScheduler: ReminderScheduler
    private lateinit var viewModel: CreateHabitViewModel

    @Before
    fun setUp() {
        dataSource = FakeHabitLocalDataSource()
        reminderScheduler = mockk {
            every { schedule(any(), any(), any(), any()) } just runs
            every { cancel(any()) } just runs
        }
        viewModel = CreateHabitViewModel(
            createHabitUseCase = CreateHabitUseCase(dataSource, reminderScheduler)
        )
    }

    @Test
    fun `initial state has default values`() = runTest {
        val state = viewModel.state.value

        assertTrue(state.name.isEmpty())
        assertNull(state.nameError)
        assertNull(state.reminderTime)
    }

    @Test
    fun `name change updates state and clears error`() = runTest {
        viewModel.onAction(CreateHabitAction.OnNameChange("Morning Run"))

        val state = viewModel.state.value
        assertTrue(state.name == "Morning Run")
        assertNull(state.nameError)
    }

    @Test
    fun `saving empty name sets name error`() = runTest {
        viewModel.onAction(CreateHabitAction.OnNameChange(""))
        viewModel.onAction(CreateHabitAction.OnSaveClick)

        assertNotNull(viewModel.state.value.nameError)
    }

    @Test
    fun `saving valid habit emits HabitCreated event`() = runTest {
        viewModel.onAction(CreateHabitAction.OnNameChange("Run"))
        viewModel.events.test {
            viewModel.onAction(CreateHabitAction.OnSaveClick)

            assertIs<CreateHabitEvent.HabitCreated>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saving valid habit adds it to repository`() = runTest {
        viewModel.onAction(CreateHabitAction.OnNameChange("Meditate"))
        viewModel.onAction(CreateHabitAction.OnSaveClick)

        val habits = dataSource.observeHabits().first()
        assertTrue(habits.any { it.name == "Meditate" })
    }

    @Test
    fun `reminder toggle enables reminder with default time`() = runTest {
        viewModel.onAction(CreateHabitAction.OnReminderToggle)

        val state = viewModel.state.value
        assertTrue(state.isReminderEnabled)
        assertNotNull(state.reminderTime)
    }

    @Test
    fun `reminder toggle twice disables reminder`() = runTest {
        viewModel.onAction(CreateHabitAction.OnReminderToggle)
        viewModel.onAction(CreateHabitAction.OnReminderToggle)

        val state = viewModel.state.value
        assertTrue(!state.isReminderEnabled)
        assertNull(state.reminderTime)
    }

    @Test
    fun `OnBackClick emits NavigateBack event`() = runTest {
        viewModel.events.test {
            viewModel.onAction(CreateHabitAction.OnBackClick)

            assertIs<CreateHabitEvent.NavigateBack>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `name too long sets name error`() = runTest {
        viewModel.onAction(CreateHabitAction.OnNameChange("a".repeat(51)))
        viewModel.onAction(CreateHabitAction.OnSaveClick)

        assertNotNull(viewModel.state.value.nameError)
    }
}
