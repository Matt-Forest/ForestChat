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
package com.forest.forestchat.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.forest.forestchat.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NavigationActivity : AppCompatActivity() {

    companion object {
        const val ThreadId = "threadId"
        const val ConversationThreadId = "conversationThreadId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = when (resources.getBoolean(R.bool.isTablet)) {
            true -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
            false -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }

        setContentView(R.layout.activity_navigation)
    }

}