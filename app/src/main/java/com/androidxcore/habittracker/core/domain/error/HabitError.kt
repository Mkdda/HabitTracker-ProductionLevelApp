package com.androidxcore.habittracker.core.domain.error

import com.androidxcore.habittracker.core.domain.util.Error

enum class HabitError : Error {
    EMPTY_NAME,
    NAME_TOO_LONG,
    INVALID_FREQUENCY
}
