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
import com.forest.forestchat.domain.useCases.GetConversationUseCase
import com.forest.forestchat.domain.useCases.SyncMessageFromUriUseCase
import com.forest.forestchat.domain.useCases.UpdateLastMessageConversationUseCase
import com.forest.forestchat.manager.ForestChatShortCutManager
import com.forest.forestchat.manager.NotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class MmsUpdatedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var forestChatShortCutManager: ForestChatShortCutManager

    @Inject
    lateinit var syncMessageFromUriUseCase: SyncMessageFromUriUseCase

    @Inject
    lateinit var getConversationUseCase: GetConversationUseCase

    @Inject
    lateinit var updateLastMessageConversationUseCase: UpdateLastMessageConversationUseCase


    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.getStringExtra("uri")?.let { uriString ->
            CoroutineScope(Dispatchers.IO).launch {
                syncMessageFromUriUseCase(Uri.parse(uriString))?.let { message ->
                    getConversationUseCase(message.threadId)?.let { conversation ->
                        updateLastMessageConversationUseCase(conversation)
                        notificationManager.update(conversation.id)
                    }
                }

                forestChatShortCutManager.updateShortcuts()
                forestChatShortCutManager.updateBadge()
                EventBus.getDefault().post(TransversalBusEvent.RefreshMessages)
            }
        }
    }

}