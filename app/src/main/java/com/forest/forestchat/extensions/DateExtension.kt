/*
 * Copyright (C) 2021 Matthieu Bouquet <matthieu@forestchat.org>
 *
 * This file is part of ForestChat.
 *
 * ForestChat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ForestChat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ForestChat.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.forest.forestchat.extensions

import android.content.Context
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Formats the [pattern] correctly for the current locale, and replaces 12 hour format with
 * 24 hour format if necessary
 */
private fun getFormatter(context: Context, pattern: String): SimpleDateFormat {
    var formattedPattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), pattern)

    if (DateFormat.is24HourFormat(context)) {
        formattedPattern = formattedPattern
            .replace("h", "HH")
            .replace("K", "HH")
            .replace(" a".toRegex(), "")
    }

    return SimpleDateFormat(formattedPattern, Locale.getDefault())
}

fun Long.getConversationTimestamp(context: Context): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance()
    then.timeInMillis = this

    return when {
        now.isSameDay(then) -> getFormatter(context, "h:mm a")
        now.isSameWeek(then) -> getFormatter(context, "E")
        now.isSameYear(then) -> getFormatter(context, "MMM d")
        else -> getFormatter(context, "MM/d/yy")
    }.format(this)
}

fun Long.isSameDayWithOther(other: Long) : Boolean {
    val firstDate = Calendar.getInstance()
    firstDate.timeInMillis = this
    val otherDate = Calendar.getInstance()
    otherDate.timeInMillis = other

    return firstDate.isSameDay(otherDate)
}

fun Long.getMessageHours(context: Context): String =
    getFormatter(context, "h:mm a").format(this)

fun Long.getMessageDate(context: Context): String? {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance()
    then.timeInMillis = this

    return when (now.isSameDay(then)) {
        true -> null
        false -> getFormatter(context, "MMM d").format(this)
    }
}