package com.androidxcore.habittracker.viewmodel

import app.cash.turbine.test
import com.androidxcore.habittracker.core.domain.model.Habit
import com.androidxcore.habittracker.core.domain.usecase.CalculateStreakUseCase
import com.androidxcore.habittracker.core.domain.usecase.CompleteHabitUseCase
import com.androidxcore.habittracker.core.domain.usecase.GetHabitsUseCase
import com.androidxcore.habittracker.core.domain.usecase.IsHabitCompletedTodayUseCase
import com.androidxcore.habittracker.fake.FakeHabitLocalDataSource
import com.androidxcore.habittracker.feature.habits.presentation.HabitsListAction
import com.androidxcore.habittracker.feature.habits.presentation.HabitsListEvent
import com.androidxcore.habittracker.feature.habits.presentation.HabitsListViewModel
import com.androidxcore.habittracker.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class HabitsListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dataSource: FakeHabitLocalDataSource
    private lateinit var viewModel: HabitsListViewModel

    private fun makeHabit(id: String = "habit-1", name: String = "Run") = Habit(
        id = id,
        name = name,
        emoji = "🏃",
        frequencyPerWeek = 7,
        reminderTime = null,
        createdAt = System.currentTimeMillis() - 86_400_000L
    )

    @Before
    fun setUp() {
        dataSource = FakeHabitLocalDataSource()
        viewModel = HabitsListViewModel(
            getHabitsUseCase = GetHabitsUseCase(dataSource),
            completeHabitUseCase = CompleteHabitUseCase(dataSource),
            calculateStreakUseCase = CalculateStreakUseCase(dataSource),
            isCompletedTodayUseCase = IsHabitCompletedTodayUseCase(dataSource)
        )
    }

    @Test
    fun `initial state has empty habits list and is not loading`() = runTest {
        val state = viewModel.state.value

        assertTrue(state.habits.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `habits appear in state when added to data source`() = runTest {
        val habit = makeHabit()
        dataSource.addHabit(habit)

        val state = viewModel.state.value

        assertEquals(1, state.habits.size)
        assertEquals("Run", state.habits.first().name)
    }

    @Test
    fun `OnHabitClick emits NavigateToDetail event`() = runTest {
        viewModel.events.test {
            viewModel.onAction(HabitsListAction.OnHabitClick("habit-1"))

            val event = awaitItem()
            assertIs<HabitsListEvent.NavigateToDetail>(event)
            assertEquals("habit-1", event.habitId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnAddHabitClick emits NavigateToCreateHabit event`() = runTest {
        viewModel.events.test {
            viewModel.onAction(HabitsListAction.OnAddHabitClick)

            assertIs<HabitsListEvent.NavigateToCreateHabit>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `completing habit already done today emits ShowError event`() = runTest {
        val habit = makeHabit()
        dataSource.addHabit(habit)
        viewModel.onAction(HabitsListAction.OnCompleteHabit(habit.id))

        viewModel.events.test {
            viewModel.onAction(HabitsListAction.OnCompleteHabit(habit.id))

            assertIs<HabitsListEvent.ShowError>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnRetry does not create duplicate habit entries`() = runTest {
        val habit = makeHabit()
        dataSource.addHabit(habit)

        viewModel.onAction(HabitsListAction.OnRetry)

        assertEquals(1, viewModel.state.value.habits.size)
    }

    @Test
    fun `completed habit is marked as completed in state`() = runTest {
        val habit = makeHabit()
        dataSource.addHabit(habit)

        viewModel.onAction(HabitsListAction.OnCompleteHabit(habit.id))

        val habitUi = viewModel.state.value.habits.first()
        assertTrue(habitUi.isCompletedToday)
    }
}
