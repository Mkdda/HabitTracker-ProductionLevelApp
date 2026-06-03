package com.androidxcore.habittracker.core.presentation

import com.androidxcore.habittracker.R
import com.androidxcore.habittracker.core.domain.error.DataError

fun DataError.toUiText(): UiText = when (this) {
    DataError.Local.DISK_FULL -> UiText.StringResource(R.string.error_disk_full)
    DataError.Local.NOT_FOUND -> UiText.StringResource(R.string.error_not_found)
    DataError.Local.UNKNOWN -> UiText.StringResource(R.string.error_unknown)
}