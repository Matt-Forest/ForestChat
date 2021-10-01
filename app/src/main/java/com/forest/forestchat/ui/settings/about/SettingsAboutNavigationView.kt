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
package com.forest.forestchat.ui.settings.about

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.navigation.findNavController
import com.forest.forestchat.databinding.NavigationSettingsAboutBinding
import com.forest.forestchat.utils.getAppVersion

class SettingsAboutNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    companion object {
        const val UrlGithub = "https://github.com/Matt-Forest/ForestChat"
        const val ContactMail = "contact@forestchat.org"
        const val Licence = "GNU General Public Licence v3.0"
        const val Copyright = "@ 2021"
    }

    lateinit var openUrl: (String) -> Unit
    lateinit var openMail: (String) -> Unit

    private val binding: NavigationSettingsAboutBinding

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationSettingsAboutBinding.inflate(layoutInflater, this)

        orientation = VERTICAL

        with(binding) {
            back.setOnClickListener { findNavController().popBackStack() }
            versionDescribe.text = context.getAppVersion()
            sourceCodeDescribe.text = UrlGithub
            contactDescribe.text = ContactMail
            licenceDescribe.text = Licence
            copyrightDescribe.text = Copyright

            sourceCode.setOnClickListener { openUrl(UrlGithub) }
            contact.setOnClickListener { openMail(ContactMail) }
        }
    }

}