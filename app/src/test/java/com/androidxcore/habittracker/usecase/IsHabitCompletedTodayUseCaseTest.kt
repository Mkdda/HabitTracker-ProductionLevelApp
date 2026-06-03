package com.androidxcore.habittracker.usecase

import com.androidxcore.habittracker.core.domain.usecase.IsHabitCompletedTodayUseCase
import com.androidxcore.habittracker.fake.FakeHabitLocalDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsHabitCompletedTodayUseCaseTest {

    private lateinit var dataSource: FakeHabitLocalDataSource
    private lateinit var useCase: IsHabitCompletedTodayUseCase

    private val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    @Before
    fun setUp() {
        dataSource = FakeHabitLocalDataSource()
        useCase = IsHabitCompletedTodayUseCase(dataSource)
    }

    @Test
    fun `returns false when no completion`() = runTest {
        val result = useCase("habit-1")

        assertFalse(result)
    }

    @Test
    fun `returns true when completed today`() = runTest {
        dataSource.addEntry("habit-1", todayStart + 1000)

        val result = useCase("habit-1")

        assertTrue(result)
    }

    @Test
    fun `returns false when only completed yesterday`() = runTest {
        val yesterday = todayStart - TimeUnit.DAYS.toMillis(1) + 1000
        dataSource.addEntry("habit-1", yesterday)

        val result = useCase("habit-1")

        assertFalse(result)
    }

    @Test
    fun `returns false on storage error instead of crashing`() = runTest {
        dataSource.addEntry("habit-1", todayStart + 1000)
        dataSource.shouldReturnError = true

        val result = useCase("habit-1")

        assertFalse(result)
    }
}
