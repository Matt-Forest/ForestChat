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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.forest.forestchat.app.ForestChatTheme
import com.forest.forestchat.localStorage.sharedPrefs.ForestChatThemeSharedPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsAppearanceViewModel @Inject constructor(
    private val themeSharedPrefs: ForestChatThemeSharedPrefs
) : ViewModel() {

    private val theme = MutableLiveData<ForestChatTheme>()
    fun theme(): LiveData<ForestChatTheme> = theme

    init {
        theme.value = themeSharedPrefs.get()
    }

    fun updateTheme(newTheme: ForestChatTheme) {
        themeSharedPrefs.set(newTheme)
        theme.value = newTheme
    }

}