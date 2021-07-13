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
import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.forest.forestchat.domain.mappers.toMessage
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.domain.models.message.MessageType
import com.forest.forestchat.localStorage.database.daos.MessageDao
import com.forest.forestchat.utils.tryOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncMessageFromUriUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getOrCreateConversationByThreadIdUseCase: GetOrCreateConversationByThreadIdUseCase,
    private val messageDao: MessageDao
) {

    suspend operator fun invoke(uri: Uri): Message? {
        // If we don't have a valid type, return null
        val type = when {
            uri.toString().contains("mms") -> MessageType.Mms
            uri.toString().contains("sms") -> MessageType.Sms
            else -> return null
        }

        // If we don't have a valid id, return null
        val id = tryOrNull { ContentUris.parseId(uri) } ?: return null

        // Check if the message already exists, so we can reuse the id
        val existingId = messageDao.getByContentId(id, type)?.id

        // The uri might be something like content://mms/inbox/id
        // The box might change though, so we should just use the mms/id uri
        val stableUri = when (type) {
            MessageType.Mms -> ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, id)
            else -> ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, id)
        }

        return context.contentResolver.query(stableUri, null, null, null, null)?.use { cursor ->

            when (cursor.moveToFirst()) {
                true -> {
                    var message = cursor.toMessage(context)
                    existingId?.let { message = message.copy(id = it) }

                    // we just want to be sure we have the conversation into the Db if not
                    // we re-sync the content provider for get all conversation.
                    getOrCreateConversationByThreadIdUseCase(message.threadId)
                    messageDao.insert(message)
                    message
                }
                false -> null
            }
        }
    }

}