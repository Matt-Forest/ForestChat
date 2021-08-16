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
package com.forest.forestchat.ui.base.fragment

import android.content.pm.ActivityInfo
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * Show fragment as immersive sticky.
 */
class FullscreenLifecycle(private val fragment: Fragment) : LifecycleObserver {

    @Suppress("DEPRECATION")
    private var baseSystemVisibilityUi : Int =
        fragment.activity?.window?.decorView?.systemUiVisibility ?: 0

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            fragment.activity?.window?.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            fragment.activity?.window?.decorView?.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            fragment.activity?.window?.setDecorFitsSystemWindows(true)
        } else {
            @Suppress("DEPRECATION")
            fragment.activity?.window?.decorView?.systemUiVisibility = baseSystemVisibilityUi
        }
    }

}

/**
 * Set orientation for a fragment
 */
class OrientationLifecycle(private val fragment: Fragment, private val orientation: Int) :
    LifecycleObserver {

    private var baseOrientation: Int =
        fragment.activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        fragment.activity?.requestedOrientation = orientation
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        fragment.activity?.requestedOrientation = baseOrientation
    }

}