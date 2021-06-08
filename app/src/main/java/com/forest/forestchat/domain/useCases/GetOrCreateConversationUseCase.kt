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
import android.telephony.PhoneNumberUtils
import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.localStorage.database.daos.ConversationDao
import com.forest.forestchat.utils.TelephonyThread
import com.forest.forestchat.utils.tryOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetOrCreateConversationUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getOrCreateConversationByThreadIdUseCase: GetOrCreateConversationByThreadIdUseCase,
    private val conversationDao: ConversationDao
) {

    suspend operator fun invoke(addresses: List<String>): Conversation? {
        if (addresses.isEmpty()) {
            return null
        }

        return (getThreadId(addresses) ?: tryOrNull {
            TelephonyThread.getOrCreateThreadId(context, addresses.toSet())
        })
            ?.takeIf { threadId -> threadId != 0L }
            ?.let { threadId ->
                getOrCreateConversationByThreadIdUseCase(threadId)
            }
    }

    private suspend fun getThreadId(recipients: List<String>): Long? {
        return conversationDao.getAll()
            ?.asSequence()
            ?.filter { conversation -> conversation.recipients.size == recipients.size }
            ?.find { conversation ->
                conversation.recipients.map { it.address }.all { address ->
                    recipients.any { recipient -> PhoneNumberUtils.compare(recipient, address) }
                }
            }
            ?.id
    }

}