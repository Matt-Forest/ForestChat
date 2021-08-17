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
package com.forest.forestchat.ui.settings.app

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.navigation.findNavController
import com.forest.forestchat.R
import com.forest.forestchat.databinding.NavigationSettingsAppBinding
import com.forest.forestchat.extensions.format
import com.forest.forestchat.utils.getAppVersion


class SettingsAppNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    lateinit var onSync: () -> Unit
    lateinit var onNotifications: () -> Unit
    lateinit var onCookiesUpdate: () -> Unit

    private val binding: NavigationSettingsAppBinding

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationSettingsAppBinding.inflate(layoutInflater, this)

        orientation = VERTICAL

        with(binding) {
            back.setOnClickListener { findNavController().popBackStack() }
            aboutDescribe.text =
                R.string.settings_app_about_describe.format(context, context.getAppVersion())
            theme.setOnClickListener {
                findNavController().navigate(SettingsAppFragmentDirections.goToAppearance())
            }
            archives.setOnClickListener {
                findNavController().navigate(SettingsAppFragmentDirections.goToArchive())
            }
            notifications.setOnClickListener { onNotifications() }
            synchronize.setOnClickListener { onSync() }
            about.setOnClickListener {
                findNavController().navigate(SettingsAppFragmentDirections.goToAbout())
            }
            cookiesSetting.setOnClickListener { onCookiesUpdate() }
        }
    }

    fun updateLoading(isLoading: Boolean) {
        binding.loadBar.apply {
            when (isLoading) {
                true -> startLoading()
                false -> stopLoading()
            }
        }
    }

}