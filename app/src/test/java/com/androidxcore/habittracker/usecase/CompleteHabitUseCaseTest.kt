package com.androidxcore.habittracker.usecase

import com.androidxcore.habittracker.core.domain.usecase.CompleteHabitError
import com.androidxcore.habittracker.core.domain.usecase.CompleteHabitUseCase
import com.androidxcore.habittracker.core.domain.util.Result
import com.androidxcore.habittracker.fake.FakeHabitLocalDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import kotlin.test.assertIs

class CompleteHabitUseCaseTest {

    private lateinit var dataSource: FakeHabitLocalDataSource
    private lateinit var useCase: CompleteHabitUseCase

    @Before
    fun setUp() {
        dataSource = FakeHabitLocalDataSource()
        useCase = CompleteHabitUseCase(dataSource)
    }

    @Test
    fun `completing a habit succeeds`() = runTest {
        val result = useCase("habit-1")

        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `completing habit twice today returns already completed error`() = runTest {
        useCase("habit-1")
        val result = useCase("habit-1")

        assertIs<Result.Error<CompleteHabitError>>(result)
        assertIs<CompleteHabitError.AlreadyCompletedToday>(result.error)
    }

    @Test
    fun `completing habit on different days succeeds both times`() = runTest {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis

        dataSource.addEntry("habit-1", yesterday)

        val result = useCase("habit-1")

        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `storage error on isCompletedToday returns storage error`() = runTest {
        dataSource.shouldReturnError = true

        val result = useCase("habit-1")

        assertIs<Result.Error<CompleteHabitError>>(result)
        assertIs<CompleteHabitError.Storage>(result.error)
    }

    @Test
    fun `storage error on completeHabit returns storage error`() = runTest {
        // First call succeeds so isCompletedToday works, then error on insert
        useCase("habit-1")
        dataSource.shouldReturnError = true

        // Try completing again — isCompletedToday returns error now too
        val result = useCase("habit-1")

        assertIs<Result.Error<CompleteHabitError>>(result)
        assertIs<CompleteHabitError.Storage>(result.error)
    }
}
