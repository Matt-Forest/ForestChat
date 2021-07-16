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
import androidx.core.app.RemoteInput
import com.forest.forestchat.app.TransversalBusEvent
import com.forest.forestchat.domain.useCases.GetConversationUseCase
import com.forest.forestchat.domain.useCases.MarkAsReadUseCase
import com.forest.forestchat.domain.useCases.SendMessageUseCase
import com.forest.forestchat.manager.ForestChatShortCutManager
import com.forest.forestchat.manager.NotificationManager
import com.forest.forestchat.manager.SubscriptionManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class ReplyReceiver : BroadcastReceiver() {

    companion object {
        const val ThreadId = "threadId"
        const val Body = "body"
    }

    @Inject
    lateinit var markAsReadUseCase: MarkAsReadUseCase

    @Inject
    lateinit var getConversationUseCase: GetConversationUseCase

    @Inject
    lateinit var subscriptionManagerCompat: SubscriptionManagerCompat

    @Inject
    lateinit var sendMessageUseCase: SendMessageUseCase

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var forestChatShortCutManager: ForestChatShortCutManager

    override fun onReceive(context: Context?, intent: Intent?) {
        val threadId = intent?.getLongExtra(ThreadId, 0L) ?: return
        val body = RemoteInput.getResultsFromIntent(intent).getCharSequence(Body)?.toString() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            markAsReadUseCase(threadId)
            notificationManager.update(threadId)
            forestChatShortCutManager.updateBadge()

            val conversation = getConversationUseCase(threadId)
            val lastMessage = conversation?.lastMessage
            val subId = subscriptionManagerCompat.activeSubscriptionInfoList
                .firstOrNull { it.subscriptionId == lastMessage?.subId }
                ?.subscriptionId ?: -1
            val addresses = conversation?.recipients?.map { it.address } ?: return@launch

            sendMessageUseCase(subId, threadId, addresses, body, listOf())
            EventBus.getDefault().post(TransversalBusEvent.RefreshMessages)
        }
    }

}