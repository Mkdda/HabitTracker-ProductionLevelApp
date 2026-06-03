package com.androidxcore.habittracker.feature.habits.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidxcore.habittracker.R
import com.androidxcore.habittracker.core.presentation.ObserveAsEvents
import com.androidxcore.habittracker.feature.habits.presentation.model.HabitUi

@Composable
fun HabitsListRoot(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToStats: () -> Unit,
    viewModel: HabitsListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is HabitsListEvent.NavigateToDetail -> onNavigateToDetail(event.habitId)
            is HabitsListEvent.NavigateToCreateHabit -> onNavigateToCreate()
            is HabitsListEvent.ShowError -> {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }

    HabitsListScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onNavigateToStats = onNavigateToStats
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsListScreen(
    state: HabitsListState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (HabitsListAction) -> Unit,
    onNavigateToStats: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onNavigateToStats) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = stringResource(R.string.cd_stats)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAction(HabitsListAction.OnAddHabitClick) }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_add_habit)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> ErrorState(
                    message = state.error.asString(LocalContext.current),
                    onRetry = { onAction(HabitsListAction.OnRetry) }
                )
                state.habits.isEmpty() -> EmptyState()
                else -> HabitsList(
                    habits = state.habits,
                    onHabitClick = { onAction(HabitsListAction.OnHabitClick(it)) },
                    onCompleteHabit = { onAction(HabitsListAction.OnCompleteHabit(it)) }
                )
            }
        }
    }
}

@Composable
private fun HabitsList(
    habits: List<HabitUi>,
    onHabitClick: (String) -> Unit,
    onCompleteHabit: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(habits, key = { it.id }) { habit ->
            HabitCard(
                habit = habit,
                onClick = { onHabitClick(habit.id) },
                onComplete = { onCompleteHabit(habit.id) }
            )
        }
    }
}

@Composable
private fun HabitCard(
    habit: HabitUi,
    onClick: () -> Unit,
    onComplete: () -> Unit
) {
    Card(onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = habit.emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(text = habit.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "🔥 ${habit.currentStreak} ${habit.streakUnit} streak",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Checkbox(
                checked = habit.isCompletedToday,
                onCheckedChange = { if (!habit.isCompletedToday) onComplete() }
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "😕", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.label_try_again))
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🌱", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.label_no_habits_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.label_no_habits_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HabitsListScreenPreview() {
    HabitsListScreen(
        state = HabitsListState(
            habits = listOf(
                HabitUi("1", "Meditate", "🧘", 7, "days", false, "Daily"),
                HabitUi("2", "Read", "📚", 3, "days", true, "Daily"),
                HabitUi("3", "Exercise", "💪", 14, "days", false, "5x per week")
            )
        ),
        onAction = {},
        onNavigateToStats = {}
    )
}
