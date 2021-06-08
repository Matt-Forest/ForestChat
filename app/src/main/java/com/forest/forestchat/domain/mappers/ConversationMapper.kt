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
package com.forest.forestchat.domain.mappers

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.domain.models.Recipient
import com.forest.forestchat.domain.models.contact.Contact
import com.forest.forestchat.domain.models.message.Message

fun Cursor.toConversation(
    context: Context,
    conversationsPersisted: List<Conversation>?,
    contacts: List<Contact>?,
    messages: List<Message>?
): Conversation {
    val id = getLong(getColumnIndex(Telephony.Threads._ID))
    val persisted = conversationsPersisted?.firstOrNull { it.id == id }
    return Conversation(
        id = id,
        archived = persisted?.archived ?: false,
        blocked = persisted?.blocked ?: false,
        pinned = persisted?.pinned ?: false,
        recipients = getRecipientByIds(
            context,
            getString(getColumnIndex(Telephony.Threads.RECIPIENT_IDS))
                .split(" ")
                .filter { it.isNotBlank() },
            contacts
        ),
        lastMessage = messages?.sortedByDescending { it.date }?.firstOrNull { it.threadId == id },
        draft = persisted?.draft,
        name = persisted?.name,
    )
}

private fun getRecipientByIds(
    context: Context,
    ids: List<String>,
    contacts: List<Contact>?
): List<Recipient> {
    val result = mutableListOf<Recipient>()

    ids.forEach { id ->
        context.contentResolver.query(
            Uri.parse("content://mms-sms/canonical-addresses"),
            null,
            "_id = ?",
            arrayOf(id),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                result.add(cursor.toRecipient(contacts))
            }
        }
    }

    return result
}