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
package com.forest.forestchat.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.forest.forestchat.R
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.domain.models.message.MessageType
import com.forest.forestchat.extensions.asString
import com.forest.forestchat.extensions.makeToast
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CopyIntoClipboard @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun copyMessage(message: Message) {
        if (message.type == MessageType.Sms) {
            val contentToCopy = message.getText()

            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("SMS", contentToCopy)
            clipboard.setPrimaryClip(clip)

            context.makeToast(R.string.copy_message.asString(context))
        }
    }

    fun copy(contentToCopy: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("copy", contentToCopy)
        clipboard.setPrimaryClip(clip)

        context.makeToast(R.string.copy_value.asString(context))
    }

}