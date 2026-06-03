package com.androidxcore.habittracker.usecase

import com.androidxcore.habittracker.core.domain.model.Habit
import com.androidxcore.habittracker.core.domain.repository.ReminderScheduler
import com.androidxcore.habittracker.core.domain.usecase.DeleteHabitUseCase
import com.androidxcore.habittracker.core.domain.util.Result
import com.androidxcore.habittracker.fake.FakeHabitLocalDataSource
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DeleteHabitUseCaseTest {

    private lateinit var dataSource: FakeHabitLocalDataSource
    private lateinit var reminderScheduler: ReminderScheduler
    private lateinit var useCase: DeleteHabitUseCase

    private val habit = Habit(
        id = "habit-1",
        name = "Run",
        emoji = "🏃",
        frequencyPerWeek = 7,
        reminderTime = "08:00",
        createdAt = System.currentTimeMillis()
    )

    @Before
    fun setUp() {
        dataSource = FakeHabitLocalDataSource()
        reminderScheduler = mockk {
            every { cancel(any()) } just runs
        }
        useCase = DeleteHabitUseCase(dataSource, reminderScheduler)
    }

    @Test
    fun `deleting habit removes it from repository`() = runTest {
        dataSource.addHabit(habit)

        val result = useCase(habit.id)

        assertIs<Result.Success<Unit>>(result)
        assertTrue(dataSource.observeHabits().first().isEmpty())
    }

    @Test
    fun `deleting habit cancels reminder`() = runTest {
        dataSource.addHabit(habit)

        useCase(habit.id)

        verify { reminderScheduler.cancel(habit.id) }
    }

    @Test
    fun `storage error returns error result`() = runTest {
        dataSource.shouldReturnError = true

        val result = useCase(habit.id)

        assertIs<Result.Error<*>>(result)
    }

    @Test
    fun `reminder is cancelled even when storage fails`() = runTest {
        dataSource.shouldReturnError = true

        useCase(habit.id)

        verify { reminderScheduler.cancel(habit.id) }
    }
}
