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

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.Telephony
import com.forest.forestchat.localStorage.database.daos.ConversationDao
import com.forest.forestchat.localStorage.database.daos.MessageDao
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkAsReadUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) {

    suspend operator fun invoke(threadId: Long) {
        messageDao.getAllByThreadId(threadId)
            ?.filter { message -> !message.read || !message.seen }
            ?.forEach { message ->
                messageDao.insert(
                    message.copy(
                        read = true,
                        seen = true
                    )
                )
            }

        conversationDao.getConversationById(threadId)?.let { conversation ->
            conversationDao.insert(
                conversation.copy(
                    lastMessage = conversation.lastMessage?.copy(
                        read = true,
                        seen = true
                    )
                )
            )
        }

        val values = ContentValues().apply {
            put(Telephony.Sms.SEEN, true)
            put(Telephony.Sms.READ, true)
        }

        try {
            val uri =
                ContentUris.withAppendedId(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI, threadId)
            context.contentResolver.update(uri, values, "${Telephony.Sms.READ} = 0", null)
        } catch (e: Exception) {
            // Nothing
        }
    }

}