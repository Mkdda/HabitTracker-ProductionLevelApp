package com.androidxcore.habittracker.feature.habits.presentation.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidxcore.habittracker.R
import com.androidxcore.habittracker.core.presentation.ObserveAsEvents
import com.androidxcore.habittracker.feature.habits.presentation.detail.CompletionRateCard
import com.androidxcore.habittracker.feature.habits.presentation.model.HabitStatsUi
import com.androidxcore.habittracker.feature.habits.presentation.model.OverallStatsUi
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape

@Composable
fun StatsRoot(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is StatsEvent.NavigateBack -> onNavigateBack()
        }
    }

    StatsScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    state: StatsState,
    onAction: (StatsAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_stats)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(StatsAction.OnBackClick) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                state.habits.isEmpty() -> {
                    EmptyStatsState(Modifier.align(Alignment.Center))
                }
                else -> {
                    StatsContent(
                        overallStats = state.overallStats!!,
                        habits = state.habits
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsContent(
    overallStats: OverallStatsUi,
    habits: List<HabitStatsUi>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { OverallStatsSection(overallStats) }
        item {
            Text(
                stringResource(R.string.label_habits_this_week),
                style = MaterialTheme.typography.titleMedium
            )
        }
        items(habits, key = { it.id }) { habit ->
            HabitStatsCard(habit = habit)
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun OverallStatsSection(stats: OverallStatsUi) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.label_overview), style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OverviewCard(
                modifier = Modifier.weight(1f),
                emoji = "✅",
                label = stringResource(R.string.label_today),
                value = "${stats.completedToday}/${stats.totalHabits}"
            )
            OverviewCard(
                modifier = Modifier.weight(1f),
                emoji = "📊",
                label = stringResource(R.string.label_avg_rate),
                value = "${(stats.averageCompletionRate * 100).toInt()}%"
            )
        }

        if (stats.bestStreak > 0) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            stringResource(R.string.label_best_streak_overview),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            stats.bestStreakHabitName,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        "${stats.bestStreak} ${stats.bestStreakUnit}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    value: String
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HabitStatsCard(habit: HabitStatsUi) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val modelProducer = remember(habit.id) {
        CartesianChartModelProducer()
    }

    LaunchedEffect(habit.completionsLast7Days) {
        modelProducer.runTransaction {
            columnSeries {
                series(habit.completionsLast7Days.map { if (it) 1.0 else 0.2 })
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(habit.emoji, style = MaterialTheme.typography.titleLarge)
                    Text(habit.name, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    "🔥 ${habit.currentStreak} ${habit.streakUnit}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                            habit.completionsLast7Days.map { completed ->
                                LineComponent(
                                    fill = fill(if (completed) primaryColor else surfaceVariant),
                                    thicknessDp = 16f,
                                    shape = CorneredShape.rounded(4)
                                )
                            }
                        )
                    )
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("6d", "5d", "4d", "3d", "2d", "1d", stringResource(R.string.label_today)).forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            CompletionRateCard(completionRate = habit.completionRate)
        }
    }
}

@Composable
private fun EmptyStatsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("📊", style = MaterialTheme.typography.displayMedium)
        Text(
            stringResource(R.string.label_no_stats_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            stringResource(R.string.label_no_stats_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsScreenPreview() {
    StatsScreen(
        state = StatsState(
            isLoading = false,
            overallStats = OverallStatsUi(
                totalHabits = 3,
                completedToday = 2,
                averageCompletionRate = 0.72f,
                bestStreakHabitName = "Morning Run",
                bestStreak = 14,
                bestStreakUnit = "days"
            ),
            habits = listOf(
                HabitStatsUi(
                    id = "1",
                    name = "Morning Run",
                    emoji = "🏃",
                    currentStreak = 14,
                    streakUnit = "days",
                    completionRate = 0.85f,
                    completionsLast7Days = listOf(true, false, true, true, true, false, true)
                )
            )
        ),
        onAction = {}
    )
}
