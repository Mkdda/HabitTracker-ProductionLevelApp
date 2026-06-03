package com.androidxcore.habittracker.usecase

import com.androidxcore.habittracker.core.domain.usecase.CalculateStreakUseCase
import com.androidxcore.habittracker.fake.FakeHabitLocalDataSource
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class CalculateStreakUseCaseTest {

    private lateinit var dataSource: FakeHabitLocalDataSource
    private lateinit var useCase: CalculateStreakUseCase

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
        useCase = CalculateStreakUseCase(dataSource)
    }

    @Test
    fun `no entries returns zero streak`() = runTest {
        val result = useCase("habit-1", daysAgo(30), 7)

        assertEquals(0, result.currentStreak)
        assertEquals(0, result.longestStreak)
        assertEquals(0f, result.completionRate)
    }

    @Test
    fun `single completion today returns streak of 1`() = runTest {
        dataSource.addEntry("habit-1", todayStart + 1000)

        val result = useCase("habit-1", daysAgo(7), 7)

        assertEquals(1, result.currentStreak)
        assertEquals(1, result.longestStreak)
        assertEquals("days", result.streakUnit)
    }

    @Test
    fun `3 consecutive days returns streak of 3`() = runTest {
        dataSource.addEntry("habit-1", daysAgo(2) + 1000)
        dataSource.addEntry("habit-1", daysAgo(1) + 1000)
        dataSource.addEntry("habit-1", todayStart + 1000)

        val result = useCase("habit-1", daysAgo(7), 7)

        assertEquals(3, result.currentStreak)
        assertEquals(3, result.longestStreak)
    }

    @Test
    fun `gap in streak resets current streak`() = runTest {
        dataSource.addEntry("habit-1", daysAgo(5) + 1000)
        dataSource.addEntry("habit-1", daysAgo(4) + 1000)

        dataSource.addEntry("habit-1", daysAgo(2) + 1000)
        dataSource.addEntry("habit-1", daysAgo(1) + 1000)
        dataSource.addEntry("habit-1", todayStart + 1000)

        val result = useCase("habit-1", daysAgo(7), 7)

        assertEquals(3, result.currentStreak)
        assertEquals(3, result.longestStreak)
    }

    @Test
    fun `longest streak tracks historical best`() = runTest {
        dataSource.addEntry("habit-1", daysAgo(10) + 1000)
        dataSource.addEntry("habit-1", daysAgo(9) + 1000)
        dataSource.addEntry("habit-1", daysAgo(8) + 1000)
        dataSource.addEntry("habit-1", daysAgo(7) + 1000)
        dataSource.addEntry("habit-1", daysAgo(6) + 1000)
        // gap
        dataSource.addEntry("habit-1", daysAgo(1) + 1000)
        dataSource.addEntry("habit-1", todayStart + 1000)

        val result = useCase("habit-1", daysAgo(14), 7)

        assertEquals(2, result.currentStreak)
        assertEquals(5, result.longestStreak)
    }

    @Test
    fun `not completing today breaks streak`() = runTest {
        dataSource.addEntry("habit-1", daysAgo(3) + 1000)
        dataSource.addEntry("habit-1", daysAgo(2) + 1000)

        val result = useCase("habit-1", daysAgo(7), 7)

        assertEquals(0, result.currentStreak)
        assertEquals(2, result.longestStreak)
    }

    @Test
    fun `completion rate is calculated correctly`() = runTest {
        dataSource.addEntry("habit-1", daysAgo(6) + 1000)
        dataSource.addEntry("habit-1", daysAgo(3) + 1000)
        dataSource.addEntry("habit-1", todayStart + 1000)

        val result = useCase("habit-1", daysAgo(7), 7)

        assertEquals(1, result.currentStreak)
        assertTrue(result.completionRate > 0f)
        assertTrue(result.completionRate <= 1f)
    }

    @Test
    fun `weekly habit 3x per week with enough completions has streak of 1 week`() = runTest {
        dataSource.addEntry("habit-1", daysAgo(2) + 1000)
        dataSource.addEntry("habit-1", daysAgo(1) + 1000)
        dataSource.addEntry("habit-1", todayStart + 1000)

        val result = useCase("habit-1", daysAgo(14), 3)

        assertEquals(1, result.currentStreak)
        assertEquals("weeks", result.streakUnit)
    }

    @Test
    fun `weekly habit insufficient completions has streak of 0`() = runTest {
        dataSource.addEntry("habit-1", todayStart + 1000)

        val result = useCase("habit-1", daysAgo(14), 3)

        assertEquals(0, result.currentStreak)
    }
}
