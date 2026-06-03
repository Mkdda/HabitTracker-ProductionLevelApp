package com.androidxcore.habittracker.feature.habits.presentation.create

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidxcore.habittracker.R
import com.androidxcore.habittracker.core.presentation.ObserveAsEvents
import com.androidxcore.habittracker.feature.habits.presentation.model.toFrequencyLabel

@Composable
fun CreateHabitRoot(
    onNavigateBack: () -> Unit,
    onHabitCreated: () -> Unit,
    viewModel: CreateHabitViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is CreateHabitEvent.NavigateBack -> onNavigateBack()
            is CreateHabitEvent.HabitCreated -> onHabitCreated()
        }
    }

    CreateHabitScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitScreen(
    state: CreateHabitState,
    onAction: (CreateHabitAction) -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_create_habit)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(CreateHabitAction.OnBackClick) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            EmojiAndNameSection(
                emoji = state.emoji,
                name = state.name,
                nameError = state.nameError?.asString(context),
                onEmojiChange = { onAction(CreateHabitAction.OnEmojiChange(it)) },
                onNameChange = { onAction(CreateHabitAction.OnNameChange(it)) }
            )

            FrequencySection(
                frequency = state.frequencyPerWeek,
                onFrequencyChange = { onAction(CreateHabitAction.OnFrequencyChange(it)) }
            )

            ReminderSection(
                isEnabled = state.isReminderEnabled,
                reminderTime = state.reminderTime,
                onToggle = { onAction(CreateHabitAction.OnReminderToggle) },
                onTimeChange = { onAction(CreateHabitAction.OnReminderTimeChange(it)) }
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { onAction(CreateHabitAction.OnSaveClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.label_save_habit))
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EmojiAndNameSection(
    emoji: String,
    name: String,
    nameError: String?,
    onEmojiChange: (String) -> Unit,
    onNameChange: (String) -> Unit
) {
    val habitEmojis = listOf(
        "🎯", "💪", "📚", "🧘", "🏃", "💧", "🥗", "😴",
        "✍️", "🎨", "🎵", "🌿", "🧹", "💊", "🙏", "🌅"
    )
    var showEmojiPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.label_habit), style = MaterialTheme.typography.titleMedium)

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { showEmojiPicker = !showEmojiPicker },
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, style = MaterialTheme.typography.headlineSmall)
            }

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.label_habit_name)) },
                placeholder = { Text(stringResource(R.string.label_habit_name_placeholder)) },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        if (showEmojiPicker) {
            EmojiGrid(
                emojis = habitEmojis,
                selectedEmoji = emoji,
                onEmojiSelected = {
                    onEmojiChange(it)
                    showEmojiPicker = false
                }
            )
        }
    }
}

@Composable
private fun EmojiGrid(
    emojis: List<String>,
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(8.dp)) {
            emojis.chunked(8).forEach { row ->
                Row {
                    row.forEach { e ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onEmojiSelected(e) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = e,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (e == selectedEmoji)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FrequencySection(
    frequency: Int,
    onFrequencyChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.label_frequency), style = MaterialTheme.typography.titleMedium)
            Text(
                text = frequency.toFrequencyLabel(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = frequency.toFloat(),
            onValueChange = { onFrequencyChange(it.toInt()) },
            valueRange = 1f..7f,
            steps = 5
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                stringResource(R.string.label_frequency_min),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.label_frequency_max),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderSection(
    isEnabled: Boolean,
    reminderTime: String?,
    onToggle: () -> Unit,
    onTimeChange: (String) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    stringResource(R.string.label_daily_reminder),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.label_reminder_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = isEnabled, onCheckedChange = { onToggle() })
        }

        if (isEnabled && reminderTime != null) {
            val parts = reminderTime.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val timePickerState = rememberTimePickerState(
                initialHour = hour,
                initialMinute = minute,
                is24Hour = true
            )

            OutlinedCard(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.label_reminder_time),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = reminderTime,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    title = { Text(stringResource(R.string.label_select_time)) },
                    text = { TimePicker(state = timePickerState) },
                    confirmButton = {
                        TextButton(onClick = {
                            onTimeChange(
                                "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
                            )
                            showTimePicker = false
                        }) {
                            Text(stringResource(R.string.label_ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text(stringResource(R.string.label_cancel))
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateHabitScreenPreview() {
    CreateHabitScreen(
        state = CreateHabitState(),
        onAction = {}
    )
}
