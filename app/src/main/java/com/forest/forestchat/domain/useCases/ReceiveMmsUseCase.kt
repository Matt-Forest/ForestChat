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

import android.net.Uri
import com.forest.forestchat.domain.models.Conversation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiveMmsUseCase @Inject constructor(
    private val syncMessageFromUriUseCase: SyncMessageFromUriUseCase,
    private val isBlockedNumbersFromProviderUseCase: IsBlockedNumbersFromProviderUseCase,
    private val markAsReadUseCase: MarkAsReadUseCase,
    private val getConversationUseCase: GetConversationUseCase,
    private val updateLastMessageConversationUseCase: UpdateLastMessageConversationUseCase
) {

    suspend operator fun invoke(uri: Uri): Conversation? =
        syncMessageFromUriUseCase(uri)?.let { message ->
            var conversation = getConversationUseCase(message.threadId)
            val isBlocked : Boolean = message.address?.let { isBlockedNumbersFromProviderUseCase(it) } == true

            if (isBlocked) {
                markAsReadUseCase(message.threadId)
            }

            // In case the conversation is one to one. We can block/unblock the conversation
            if (conversation?.recipients?.size == 1) {
                conversation = conversation.copy(blocked = isBlocked)
            }

            conversation?.let { updateLastMessageConversationUseCase(it) }
            return when (conversation?.blocked == true) {
                true -> null
                false -> {
                    conversation?.copy(archived = false)
                }
            }
        }

}