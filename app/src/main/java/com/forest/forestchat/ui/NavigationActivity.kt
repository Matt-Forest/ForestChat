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

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.forest.forestchat.R
import com.forest.forestchat.extensions.asColor
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
    }

    override fun onResume() {
        super.onResume()
        updateStatusBarTheme()
        updateNavigationBarTheme()
    }

    private fun updateStatusBarTheme() {
        with(window) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                with(decorView) {
                    systemUiVisibility =
                        when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                statusBarColor = R.color.richeBlack.asColor(context)
                                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                            }
                            else -> {
                                statusBarColor = R.color.white.asColor(context)
                                systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            }
                        }
                }
            }
        }
    }

    private fun updateNavigationBarTheme() {
        with(window) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                with(decorView) {
                    systemUiVisibility =
                        when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                navigationBarColor = R.color.richeBlack.asColor(context)
                                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                            }
                            else -> {
                                navigationBarColor = R.color.white.asColor(context)
                                systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                            }
                        }
                }
            }
        }
    }

}