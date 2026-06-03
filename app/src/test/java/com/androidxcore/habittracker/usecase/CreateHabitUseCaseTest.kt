package com.androidxcore.habittracker.usecase

import com.androidxcore.habittracker.core.domain.error.HabitError
import com.androidxcore.habittracker.core.domain.repository.ReminderScheduler
import com.androidxcore.habittracker.core.domain.usecase.CreateHabitError
import com.androidxcore.habittracker.core.domain.usecase.CreateHabitUseCase
import com.androidxcore.habittracker.fake.FakeHabitLocalDataSource
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertIs
import com.androidxcore.habittracker.core.domain.util.Result
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateHabitUseCaseTest {

    private lateinit var dataSource: FakeHabitLocalDataSource
    private lateinit var reminderScheduler: ReminderScheduler
    private lateinit var useCase: CreateHabitUseCase

    @Before
    fun setUp() {
        dataSource = FakeHabitLocalDataSource()
        // ReminderScheduler sí lo mockeamos — es una interfaz con side effects
        reminderScheduler = mockk {
            every { schedule(any(), any(), any(), any()) } just runs
            every { cancel(any()) } just runs
        }
        useCase = CreateHabitUseCase(dataSource, reminderScheduler)
    }

    @Test
    fun `empty name returns validation error`() = runTest {
        val result = useCase(
            name = "",
            emoji = "🎯",
            frequencyPerWeek = 7,
            reminderTime = null
        )

        assertIs<Result.Error<CreateHabitError>>(result)
        assertIs<CreateHabitError.Validation>(result.error)
        assertEquals(HabitError.EMPTY_NAME, result.error.error)
    }

    @Test
    fun `blank name returns validation error`() = runTest {
        val result = useCase(
            name = "   ",
            emoji = "🎯",
            frequencyPerWeek = 7,
            reminderTime = null
        )

        assertIs<Result.Error<CreateHabitError>>(result)
        assertIs<CreateHabitError.Validation>(result.error)
        assertEquals(HabitError.EMPTY_NAME, result.error.error)
    }

    @Test
    fun `name longer than 50 chars returns validation error`() = runTest {
        val result = useCase(
            name = "a".repeat(51),
            emoji = "🎯",
            frequencyPerWeek = 7,
            reminderTime = null
        )

        assertIs<Result.Error<CreateHabitError>>(result)
        assertIs<CreateHabitError.Validation>(result.error)
        assertEquals(HabitError.NAME_TOO_LONG, result.error.error)
    }

    @Test
    fun `frequency 0 returns validation error`() = runTest {
        val result = useCase(
            name = "Run",
            emoji = "🏃",
            frequencyPerWeek = 0,
            reminderTime = null
        )

        assertIs<Result.Error<CreateHabitError>>(result)
        assertIs<CreateHabitError.Validation>(result.error)
        assertEquals(HabitError.INVALID_FREQUENCY, result.error.error)
    }

    @Test
    fun `frequency 8 returns validation error`() = runTest {
        val result = useCase(
            name = "Run",
            emoji = "🏃",
            frequencyPerWeek = 8,
            reminderTime = null
        )

        assertIs<Result.Error<CreateHabitError>>(result)
        assertIs<CreateHabitError.Validation>(result.error)
        assertEquals(HabitError.INVALID_FREQUENCY, result.error.error)
    }

    @Test
    fun `valid habit is saved successfully`() = runTest {
        val result = useCase(
            name = "Morning Run",
            emoji = "🏃",
            frequencyPerWeek = 7,
            reminderTime = null
        )

        assertIs<Result.Success<Unit>>(result)
        assertTrue(dataSource.observeHabits().first().any { it.name == "Morning Run" })
    }

    @Test
    fun `valid habit with reminder schedules notification`() = runTest {
        useCase(
            name = "Meditate",
            emoji = "🧘",
            frequencyPerWeek = 7,
            reminderTime = "08:00"
        )

        verify { reminderScheduler.schedule(any(), eq("Meditate"), eq(8), eq(0)) }
    }

    @Test
    fun `habit without reminder does not schedule notification`() = runTest {
        useCase(
            name = "Read",
            emoji = "📚",
            frequencyPerWeek = 7,
            reminderTime = null
        )

        verify(exactly = 0) { reminderScheduler.schedule(any(), any(), any(), any()) }
    }

    @Test
    fun `storage error returns storage error`() = runTest {
        dataSource.shouldReturnError = true

        val result = useCase(
            name = "Run",
            emoji = "🏃",
            frequencyPerWeek = 7,
            reminderTime = null
        )

        assertIs<Result.Error<CreateHabitError>>(result)
        assertIs<CreateHabitError.Storage>(result.error)
    }
}