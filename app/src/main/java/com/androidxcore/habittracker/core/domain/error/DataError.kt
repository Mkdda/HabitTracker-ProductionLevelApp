package com.androidxcore.habittracker.core.domain.error

import com.androidxcore.habittracker.core.domain.util.Error

sealed interface DataError : Error {
    enum class Local : DataError {
        DISK_FULL,
        NOT_FOUND,
        UNKNOWN
    }
}