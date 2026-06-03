package com.androidxcore.habittracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.androidxcore.habittracker.feature.habits.presentation.HabitsListRoot
import com.androidxcore.habittracker.feature.habits.presentation.create.CreateHabitRoot
import com.androidxcore.habittracker.feature.habits.presentation.detail.HabitDetailRoot
import com.androidxcore.habittracker.feature.habits.presentation.stats.StatsRoot

@Composable
fun NavigationRoot(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.HabitsList
    ) {
        composable<Screen.HabitsList> {
            HabitsListRoot(
                onNavigateToDetail = { habitId ->
                    navController.navigate(Screen.HabitDetail(habitId))
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.CreateHabit)
                },
                onNavigateToStats = {
                    navController.navigate(Screen.Stats)
                }
            )
        }

        composable<Screen.CreateHabit> {
            CreateHabitRoot(
                onNavigateBack = { navController.popBackStack() },
                onHabitCreated = { navController.popBackStack() }
            )
        }

        composable<Screen.HabitDetail> {
            HabitDetailRoot(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Stats> {
            StatsRoot(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}