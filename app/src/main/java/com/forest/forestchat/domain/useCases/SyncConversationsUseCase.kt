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

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.forest.forestchat.domain.mappers.toConversation
import com.forest.forestchat.domain.mappers.toRecipient
import com.forest.forestchat.domain.models.Recipient
import com.forest.forestchat.domain.models.contact.Contact
import com.forest.forestchat.extensions.getLongValue
import com.forest.forestchat.extensions.getStringValue
import com.forest.forestchat.extensions.queryCursor
import com.forest.forestchat.localStorage.database.daos.ContactDao
import com.forest.forestchat.localStorage.database.daos.ConversationDao
import com.forest.forestchat.localStorage.database.daos.MessageDao
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncConversationsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val isBlockedNumbersFromProviderUseCase: IsBlockedNumbersFromProviderUseCase,
    private val conversationDao: ConversationDao,
    private val contactDao: ContactDao,
    private val messageDao: MessageDao
) {

    suspend operator fun invoke() {
        val persistedConversationData = conversationDao.getAll()
        conversationDao.deleteAll()
        val contacts = contactDao.getAll()
        val messages = messageDao.getAll()

        context.contentResolver.query(
            Uri.parse("${Telephony.MmsSms.CONTENT_CONVERSATIONS_URI}?simple=true"),
            arrayOf(
                Telephony.Threads._ID,
                Telephony.Threads.RECIPIENT_IDS
            ),
            null,
            null,
            "date desc"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLongValue(Telephony.Threads._ID)
                val persistedConversation = persistedConversationData?.firstOrNull { it.id == id }
                val recipients = getRecipientByIds(
                    cursor.getStringValue(Telephony.Threads.RECIPIENT_IDS)
                        .split(" ")
                        .filter { it.isNotBlank() },
                    contacts
                )
                // if we have a persisted value so we get it, else we ask to provider
                val isBlocked = when (persistedConversation) {
                    null -> recipients.all { isBlockedNumbersFromProviderUseCase(it.address) }
                    else -> persistedConversation.blocked
                }

                conversationDao.insert(
                    toConversation(
                        id,
                        persistedConversation,
                        isBlocked,
                        recipients,
                        messages
                    )
                )
            }
        }
    }

    private fun getRecipientByIds(
        ids: List<String>,
        contacts: List<Contact>?
    ): List<Recipient> {
        val result = mutableListOf<Recipient>()

        ids.forEach { id ->
            context.queryCursor(
                uri = Uri.withAppendedPath(Telephony.MmsSms.CONTENT_URI, "canonical-addresses"),
                selection = "${Telephony.Mms._ID} = ?",
                selectionArgs = arrayOf(id)
            ) { cursor ->
                result.add(cursor.toRecipient(contacts))
            }
        }

        return result
    }

}