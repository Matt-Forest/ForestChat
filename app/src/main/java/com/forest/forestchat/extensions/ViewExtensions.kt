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

import android.view.View
import androidx.core.view.updatePadding

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visibleIf(f: () -> Boolean) {
    if (f()) {
        visible()
    } else {
        gone()
    }
}

fun View.invisibleIf(f: () -> Boolean) {
    if (f()) {
        invisible()
    } else {
        visible()
    }
}

fun View.goneIf(f: () -> Boolean) {
    if (f()) {
        gone()
    } else {
        visible()
    }
}

/**
 * Display given view below status bar adding additional padding.
 */
fun View.addStatusBarHeightAsPadding() {
    post {
        // create a snapshot of the view's top margin
        val initialPadding = paddingTop

        setOnApplyWindowInsetsListener { _, insets ->
            // do not forget initial padding to keep padding relative to the status bar
            val padding = initialPadding + insets.systemWindowInsetTop
            updatePadding(top = padding)
            insets
        }

        requestApplyInsetsWhenAttached()
    }
}

private fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        // We're already attached, just request as normal
        requestApplyInsets()
    } else {
        // We're not attached to the hierarchy, add a listener to
        // request when we are
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}