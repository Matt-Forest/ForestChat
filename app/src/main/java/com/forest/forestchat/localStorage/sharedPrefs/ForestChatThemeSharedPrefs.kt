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
package com.forest.forestchat.localStorage.sharedPrefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.forest.forestchat.app.ForestChatTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForestChatThemeSharedPrefs @Inject constructor(
    @ApplicationContext context: Context
) {

    private object Key {
        const val ForestChatTheme = "ForestChatTheme"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "shared_preferences_forest_chat_theme",
        Context.MODE_PRIVATE
    )

    fun get() = sharedPreferences.getString(Key.ForestChatTheme, ForestChatTheme.System.name)
        ?.let { ForestChatTheme.valueOf(it) }

    fun set(theme: ForestChatTheme) {
        sharedPreferences.edit {
            putString(Key.ForestChatTheme, theme.name)
        }
    }

}