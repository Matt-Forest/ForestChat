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
package com.forest.forestchat.services

import android.content.Intent
import android.net.Uri
import android.telephony.TelephonyManager
import androidx.core.app.JobIntentService
import com.forest.forestchat.domain.useCases.GetConversationUseCase
import com.forest.forestchat.domain.useCases.GetOrCreateConversationUseCase
import com.forest.forestchat.domain.useCases.SendMessageFromNotificationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SmsSendService : JobIntentService() {

    @Inject
    lateinit var getOrCreateConversationUseCase: GetOrCreateConversationUseCase

    @Inject
    lateinit var sendMessageFromNotificationUseCase: SendMessageFromNotificationUseCase

    override fun onHandleWork(intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_RESPOND_VIA_MESSAGE) {
            intent.extras?.getString(Intent.EXTRA_TEXT)?.takeIf { it.isNotBlank() }?.let { body ->
                GlobalScope.launch(Dispatchers.IO) {
                    val intentUri = intent.data
                    val recipients = intentUri?.let(::getRecipients)?.split(";") ?: return@launch
                    val threadId = getOrCreateConversationUseCase(recipients)?.id ?: 0L
                    sendMessageFromNotificationUseCase(-1, threadId, recipients, body)
                }
            }
        }
    }

    private fun getRecipients(uri: Uri): String {
        val base = uri.schemeSpecificPart
        val position = base.indexOf('?')
        return if (position == -1) base else base.substring(0, position)
    }

}