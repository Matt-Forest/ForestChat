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
import android.provider.Telephony
import android.telephony.SmsMessage
import androidx.core.content.contentValuesOf
import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.domain.models.message.MessageBox
import com.forest.forestchat.domain.models.message.MessageType
import com.forest.forestchat.domain.models.message.sms.MessageSms
import com.forest.forestchat.domain.models.message.sms.SmsStatus
import com.forest.forestchat.manager.ActiveThreadManager
import com.forest.forestchat.utils.TelephonyThread
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiveSmsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val updateMessageUseCase: UpdateMessageUseCase,
    private val getOrCreateConversationByThreadIdUseCase: GetOrCreateConversationByThreadIdUseCase,
    private val activeThreadManager: ActiveThreadManager
) {

    suspend operator fun invoke(subscriptionId: Int, messages: Array<SmsMessage>): Conversation? {
        if (messages.isNotEmpty()) {
            // convert to Db Message
            var message = convertToMessage(subscriptionId, messages)
            // insert the new message to the content provider and set the new contentId
            insertSmsToProvider(message)?.let { contentId ->
                message = message.copy(contentId = contentId)
            }

            val conversation = getOrCreateConversationByThreadIdUseCase(message.threadId)
            if (conversation?.blocked == true) {
                message = message.copy(
                    seen = true,
                    read = true
                )
            }

            updateMessageUseCase(message)

            return when (conversation?.blocked == true) {
                true -> null
                false -> {
                    conversation?.copy(archived = false)
                }
            }
        }

        return null
    }

    private fun convertToMessage(subscriptionId: Int, messages: Array<SmsMessage>): Message {
        val address = messages[0].displayOriginatingAddress
        val threadId = TelephonyThread.getOrCreateThreadId(context, listOf(address))
        return Message(
            threadId = threadId,
            contentId = 0L,
            address = address,
            box = MessageBox.Inbox,
            type = MessageType.Sms,
            date = System.currentTimeMillis(),
            dateSent = messages[0].timestampMillis,
            read = activeThreadManager.getActiveThread() == threadId,
            seen = false,
            subId = subscriptionId,
            sms = MessageSms(
                body = messages
                    .mapNotNull { message -> message.displayMessageBody }
                    .reduce { body, new -> body + new },
                errorCode = 0,
                deliveryStatus = SmsStatus.None,
            ),
            mms = null,
        )
    }

    /**
     * Insert the message to the native content provider
     */
    private fun insertSmsToProvider(message: Message): Long? {
        val values = contentValuesOf(
            Telephony.Sms.ADDRESS to message.address,
            Telephony.Sms.BODY to message.sms?.body,
            Telephony.Sms.DATE_SENT to message.dateSent,
            Telephony.Sms.SUBSCRIPTION_ID to message.subId
        )

        return context.contentResolver.insert(
            Telephony.Sms.Inbox.CONTENT_URI,
            values
        )?.lastPathSegment?.toLong()
    }

}