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
package com.forest.forestchat.ui.settings.appearance

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import com.forest.forestchat.app.ForestChatTheme
import com.forest.forestchat.databinding.NavigationSettingsAppearanceBinding

class SettingsAppearanceNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding: NavigationSettingsAppearanceBinding

    lateinit var onThemeChange: (ForestChatTheme) -> Unit

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationSettingsAppearanceBinding.inflate(layoutInflater, this)

        orientation = VERTICAL

        with(binding) {
            back.setOnClickListener { findNavController().popBackStack() }

            systemTheme.setOnClickListener { onThemeChange(ForestChatTheme.System) }
            systemCheckbox.setOnClickListener { onThemeChange(ForestChatTheme.System) }

            lightTheme.setOnClickListener { onThemeChange(ForestChatTheme.Light) }
            lightCheckbox.setOnClickListener { onThemeChange(ForestChatTheme.Light) }

            darkTheme.setOnClickListener { onThemeChange(ForestChatTheme.Dark) }
            darkCheckbox.setOnClickListener { onThemeChange(ForestChatTheme.Dark) }
        }
    }

    fun updateTheme(newTheme: ForestChatTheme) {
        with(binding) {
            systemCheckbox.isChecked = newTheme == ForestChatTheme.System
            lightCheckbox.isChecked = newTheme == ForestChatTheme.Light
            darkCheckbox.isChecked = newTheme == ForestChatTheme.Dark
        }
        themeSelect(newTheme)
    }

    private fun themeSelect(theme: ForestChatTheme) {
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                ForestChatTheme.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                ForestChatTheme.Light -> AppCompatDelegate.MODE_NIGHT_NO
                ForestChatTheme.Dark -> AppCompatDelegate.MODE_NIGHT_YES
            }
        )
    }

}