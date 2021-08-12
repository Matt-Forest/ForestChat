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
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.forest.forestchat.R
import com.forest.forestchat.databinding.NavigationSettingsAppBinding
import com.forest.forestchat.extensions.format


class SettingsAppNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    lateinit var onSync: () -> Unit
    lateinit var onNotifications: () -> Unit

    private val binding: NavigationSettingsAppBinding

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationSettingsAppBinding.inflate(layoutInflater, this)

        orientation = VERTICAL

        with(binding) {
            aboutDescribe.text =
                R.string.settings_app_about_describe.format(context, getAppVersion())
            theme.setOnClickListener {
                //TODO go to theme fragment
            }
            archives.setOnClickListener {
                // TODO go to archive fragment
            }
            notifications.setOnClickListener { onNotifications() }
            synchronize.setOnClickListener { onSync() }
            about.setOnClickListener {
                // TODO go to about fragment
            }
        }
    }

    private fun getAppVersion(): String =
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            ""
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