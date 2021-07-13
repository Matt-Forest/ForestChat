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
import android.net.Uri
import com.forest.forestchat.app.TransversalBusEvent
import com.forest.forestchat.domain.useCases.ReceiveMmsUseCase
import com.forest.forestchat.manager.ForestChatShortCutManager
import com.forest.forestchat.manager.NotificationManager
import com.klinker.android.send_message.MmsReceivedReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

/**
 * We need to pass by a BroadcastReceiver instead of MmsReceivedReceiver from klinker because,
 * Dagger need the onReceiver for the injection.
 */
@AndroidEntryPoint
class MmsReceivedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var receiveMmsUseCase: ReceiveMmsUseCase

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var forestChatShortCutManager: ForestChatShortCutManager


    override fun onReceive(context: Context?, intent: Intent?) {
        Receiver(object : Receiver.MmsReceiverListener {

            override fun onMessageReceived(context: Context?, messageUri: Uri?) {
                messageUri?.let { uri ->
                    CoroutineScope(Dispatchers.IO).launch {
                        receiveMmsUseCase(uri)?.let { conversation ->
                            notificationManager.update(conversation.id)

                            forestChatShortCutManager.updateShortcuts()
                            forestChatShortCutManager.updateBadge()
                            EventBus.getDefault().post(TransversalBusEvent.RefreshMessages)
                        }
                    }
                }
            }

        }).onReceive(context, intent)
    }

    private class Receiver(private val listener: MmsReceiverListener) : MmsReceivedReceiver() {

        override fun onMessageReceived(context: Context?, messageUri: Uri?) {
            listener.onMessageReceived(context, messageUri)
        }

        override fun onError(p0: Context?, p1: String?) {
            // Nothing
        }

        interface MmsReceiverListener {
            fun onMessageReceived(context: Context?, messageUri: Uri?)
        }

    }

}