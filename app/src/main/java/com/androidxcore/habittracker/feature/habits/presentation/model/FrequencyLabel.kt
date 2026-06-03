package com.androidxcore.habittracker.feature.habits.presentation.model

internal fun Int.toFrequencyLabel(): String = when (this) {
    7 -> "Daily"
    1 -> "Once a week"
    else -> "${this}x per week"
}
