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

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import com.forest.forestchat.domain.useCases.GetOrCreateConversationByAddressesUseCase
import com.forest.forestchat.domain.useCases.SendMessageUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsSendService : Service() {

    @Inject
    lateinit var getOrCreateConversationByAddressesUseCase: GetOrCreateConversationByAddressesUseCase

    @Inject
    lateinit var sendMessageUseCase: SendMessageUseCase

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_NOT_STICKY
        }

        intent.extras?.getString(Intent.EXTRA_TEXT)?.takeIf { it.isNotBlank() }?.let { body ->
            CoroutineScope(Dispatchers.IO).launch {
                val intentUri = intent.data
                val recipients = intentUri?.let(::getRecipients)?.split(";") ?: return@launch
                val threadId = getOrCreateConversationByAddressesUseCase(recipients)?.id ?: 0L
                sendMessageUseCase(-1, threadId, recipients, body)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun getRecipients(uri: Uri): String {
        val base = uri.schemeSpecificPart
        val position = base.indexOf('?')
        return if (position == -1) base else base.substring(0, position)
    }

}