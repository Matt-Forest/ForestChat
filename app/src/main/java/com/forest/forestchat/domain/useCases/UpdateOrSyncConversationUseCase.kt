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
package com.forest.forestchat.domain.useCases

import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.domain.useCases.synchronize.SyncConversationsUseCase
import javax.inject.Inject

class UpdateOrSyncConversationUseCase @Inject constructor(
    private val getConversationUseCase: GetConversationUseCase,
    private val updateConversationUseCase: UpdateConversationUseCase,
    private val syncConversationsUseCase: SyncConversationsUseCase
) {

    suspend operator fun invoke(message: Message) : Conversation? =
        when (val conversation = getConversationUseCase(message.threadId)) {
            null -> {
                syncConversationsUseCase()
                getConversationUseCase(message.threadId)?.let {
                    updateConversation(it, message)
                }
            }
            else -> {
                updateConversation(conversation, message)
            }
        }

    private suspend fun updateConversation(
        conversation: Conversation,
        message: Message
    ): Conversation {
        val conv = conversation.copy(lastMessage = message)
        updateConversationUseCase(conv)
        return conv
    }

}