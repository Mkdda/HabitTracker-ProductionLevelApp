package com.androidxcore.habittracker.usecase

import com.androidxcore.habittracker.core.domain.usecase.GetWeeklyCompletionsUseCase
import com.androidxcore.habittracker.fake.FakeHabitLocalDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetWeeklyCompletionsUseCaseTest {

    private lateinit var dataSource: FakeHabitLocalDataSource
    private lateinit var useCase: GetWeeklyCompletionsUseCase

    private val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun daysAgo(days: Int): Long =
        todayStart - TimeUnit.DAYS.toMillis(days.toLong())

    @Before
    fun setUp() {
        dataSource = FakeHabitLocalDataSource()
        useCase = GetWeeklyCompletionsUseCase(dataSource)
    }

    @Test
    fun `returns list of 7 booleans`() = runTest {
        val result = useCase("habit-1")

        assertEquals(7, result.size)
    }

    @Test
    fun `all false when no completions`() = runTest {
        val result = useCase("habit-1")

        assertTrue(result.all { !it })
    }

    @Test
    fun `today completion reflected at last position`() = runTest {
        dataSource.addEntry("habit-1", todayStart + 1000)

        val result = useCase("habit-1")

        assertTrue(result.last())
    }

    @Test
    fun `yesterday completion reflected at second-to-last position`() = runTest {
        dataSource.addEntry("habit-1", daysAgo(1) + 1000)

        val result = useCase("habit-1")

        assertTrue(result[5])
        assertFalse(result[6])
    }

    @Test
    fun `days outside the 7-day window are not counted`() = runTest {
        dataSource.addEntry("habit-1", daysAgo(8) + 1000)

        val result = useCase("habit-1")

        assertTrue(result.all { !it })
    }

    @Test
    fun `storage error returns false for that day`() = runTest {
        dataSource.addEntry("habit-1", todayStart + 1000)
        dataSource.shouldReturnError = true

        val result = useCase("habit-1")

        assertTrue(result.all { !it })
    }
}
