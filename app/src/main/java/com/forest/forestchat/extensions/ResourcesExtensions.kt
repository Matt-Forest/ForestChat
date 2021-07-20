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
import android.content.res.Resources
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

val Int.dp
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Int.sp
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity).toInt()

fun Int.asDimen(context: Context?) = context?.resources?.getDimension(this)

fun Int.asString(context: Context?) = context?.resources?.getString(this) ?: ""

fun Int.asStringArray(context: Context?): Array<String> = context?.resources?.getStringArray(this) ?: emptyArray()

fun Int.format(context: Context?, vararg args: Any?) = context?.resources?.getString(this)?.format(*args) ?: ""

fun Int.asDrawable(context: Context?) = context?.let { ContextCompat.getDrawable(it, this) }

fun Int.asPlurals(context: Context?, quantity: Int) =
    context?.resources?.getQuantityString(this, quantity, quantity) ?: ""

fun Int.asFont(context: Context?) = context?.let { ResourcesCompat.getFont(it, this) }

fun Int.asColor(context: Context?) =
    context?.let { ContextCompat.getColor(it, this) } ?: Color.BLACK