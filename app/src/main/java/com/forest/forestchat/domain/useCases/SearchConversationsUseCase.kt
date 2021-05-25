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

import com.forest.forestchat.domain.models.SearchConversationResult
import com.forest.forestchat.extensions.removeAccents
import com.forest.forestchat.localStorage.database.daos.MessageDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchConversationsUseCase @Inject constructor(
    private val conversationsUseCase: GetConversationsUseCase,
    private val messageDao: MessageDao
) {

    suspend operator fun invoke(search: String): List<SearchConversationResult>? {
        val normalizedQuery = search.removeAccents()
        conversationsUseCase()?.let { conversations ->
            return messageDao.getAll()
                ?.asSequence()
                ?.filter { message ->
                    message.getSummary().contains(normalizedQuery, true)
                }
                ?.groupBy { it.threadId }
                ?.filter { (threadId, _) -> conversations.firstOrNull { it.id == threadId } != null }
                ?.map { (threadId, messages) ->
                    Pair(
                        conversations.first { it.id == threadId },
                        messages.size
                    )
                }
                ?.map { (conversation, messages) ->
                    SearchConversationResult(
                        normalizedQuery,
                        conversation,
                        messages
                    )
                }
                ?.sortedByDescending { result -> result.messages }
                ?.toList()
        }
        return null
    }

}