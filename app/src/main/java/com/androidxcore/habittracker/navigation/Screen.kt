package com.androidxcore.habittracker.navigation
import kotlinx.serialization.Serializable

sealed interface Screen {

    @Serializable
    data object HabitsList : Screen

    @Serializable
    data class HabitDetail(val habitId: String) : Screen

    @Serializable
    data object CreateHabit : Screen

    @Serializable
    data object Stats : Screen
}