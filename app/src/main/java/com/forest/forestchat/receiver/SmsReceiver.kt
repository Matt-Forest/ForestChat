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
package com.forest.forestchat.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.forest.forestchat.app.TransversalBusEvent
import com.forest.forestchat.domain.useCases.ReceiveSmsUseCase
import com.forest.forestchat.manager.ForestChatShortCutManager
import com.forest.forestchat.manager.NotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var receiveSmsUseCase: ReceiveSmsUseCase

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var forestChatShortCutManager: ForestChatShortCutManager

    override fun onReceive(context: Context?, intent: Intent?) {
        Telephony.Sms.Intents.getMessagesFromIntent(intent)?.let { messages ->
            val subscriptionId = intent?.extras?.getInt("subscription", -1) ?: -1

            CoroutineScope(Dispatchers.IO).launch {
                receiveSmsUseCase(subscriptionId, messages)?.let { conversation ->

                    notificationManager.update(conversation.id)

                    forestChatShortCutManager.updateShortcuts()
                    forestChatShortCutManager.updateBadge()
                    EventBus.getDefault().post(TransversalBusEvent.ReceiveSms)
                }
            }
        }
    }

}