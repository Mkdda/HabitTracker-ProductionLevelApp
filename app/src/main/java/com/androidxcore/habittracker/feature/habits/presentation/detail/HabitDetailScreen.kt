package com.androidxcore.habittracker.feature.habits.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.androidxcore.habittracker.feature.habits.presentation.model.HabitDetailUi

@Composable
fun HabitDetailRoot(
    onNavigateBack: () -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is HabitDetailEvent.NavigateBack -> onNavigateBack()
            is HabitDetailEvent.ShowError -> {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }

    HabitDetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    state: HabitDetailState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (HabitDetailAction) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(state.habit?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = { onAction(HabitDetailAction.OnBackClick) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    if (state.habit != null) {
                        IconButton(onClick = { onAction(HabitDetailAction.OnDeleteClick) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.cd_delete_habit),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
                state.habit != null -> {
                    HabitDetailContent(
                        habit = state.habit,
                        isCompleting = state.isCompleting,
                        onAction = onAction
                    )
                }
                else -> {
                    HabitLoadErrorState(
                        modifier = Modifier.align(Alignment.Center),
                        onRetry = { onAction(HabitDetailAction.OnRetry) }
                    )
                }
            }
        }
    }

    if (state.showDeleteDialog) {
        DeleteConfirmDialog(
            habitName = state.habit?.name ?: "",
            onConfirm = { onAction(HabitDetailAction.OnConfirmDelete) },
            onDismiss = { onAction(HabitDetailAction.OnDismissDelete) }
        )
    }
}

@Composable
private fun HabitLoadErrorState(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("😕", style = MaterialTheme.typography.displayMedium)
        Text(
            text = stringResource(R.string.error_not_found),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text(stringResource(R.string.label_try_again))
        }
    }
}

@Composable
private fun HabitDetailContent(
    habit: HabitDetailUi,
    isCompleting: Boolean,
    onAction: (HabitDetailAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Text(text = habit.emoji, style = MaterialTheme.typography.displayLarge)
        Text(
            text = habit.name,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = habit.frequencyLabel,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.label_current_streak),
                value = "🔥 ${habit.currentStreak}",
                subtitle = habit.streakUnit
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.label_best_streak),
                value = "⭐ ${habit.longestStreak}",
                subtitle = habit.streakUnit
            )
        }

        CompletionRateCard(completionRate = habit.completionRate)

        if (habit.reminderTime != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.label_daily_reminder),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = habit.reminderTime,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { onAction(HabitDetailAction.OnCompleteClick) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !habit.isCompletedToday && !isCompleting,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (habit.isCompletedToday)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.primary
            )
        ) {
            if (isCompleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (habit.isCompletedToday)
                        stringResource(R.string.label_completed_today)
                    else
                        stringResource(R.string.label_mark_done),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
internal fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subtitle: String
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun CompletionRateCard(
    modifier: Modifier = Modifier,
    completionRate: Float
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.label_completion_rate),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${(completionRate * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            LinearProgressIndicator(
                progress = { completionRate },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    habitName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.label_delete_dialog_title)) },
        text = {
            Text(
                stringResource(R.string.label_delete_dialog_text, habitName)
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.label_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.label_cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun HabitDetailScreenPreview() {
    HabitDetailScreen(
        state = HabitDetailState(
            isLoading = false,
            habit = HabitDetailUi(
                id = "1",
                name = "Morning Run",
                emoji = "🏃",
                frequencyLabel = "Daily",
                currentStreak = 7,
                longestStreak = 14,
                streakUnit = "days",
                completionRate = 0.78f,
                isCompletedToday = false,
                reminderTime = "07:00"
            )
        ),
        onAction = {}
    )
}
