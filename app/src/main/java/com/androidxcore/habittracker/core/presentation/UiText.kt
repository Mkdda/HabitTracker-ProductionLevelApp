package com.androidxcore.habittracker.core.presentation

import android.content.Context
import androidx.annotation.StringRes

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class StringResource(
        @StringRes val id: Int,
        val args: Array<Any> = emptyArray()
    ) : UiText

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(id, *args)
        }
    }
}