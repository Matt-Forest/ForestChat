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
import com.forest.forestchat.localStorage.database.daos.ConversationDao
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPinnedShortCutConversationUseCase @Inject constructor(
    private val conversationDao: ConversationDao
) {

    suspend operator fun invoke(): List<Conversation>? =
        conversationDao.getAll()
            ?.filter {
                it.lastMessage != null
                        && !it.archived
                        && !it.blocked
                        && it.recipients.isNotEmpty()
                        && (it.pinned || it.lastMessage.date > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(
                    7
                ))
            }
            ?.sortedByDescending { it.lastMessage?.date }

}