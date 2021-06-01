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

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsManager
import androidx.core.content.contentValuesOf
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.domain.models.message.MessageBox
import com.forest.forestchat.domain.models.message.MessageType
import com.forest.forestchat.domain.models.message.sms.MessageSms
import com.forest.forestchat.domain.models.message.sms.SmsStatus
import com.forest.forestchat.domain.useCases.synchronize.SyncConversationsUseCase
import com.forest.forestchat.domain.useCases.synchronize.SyncMessagesUseCase
import com.forest.forestchat.manager.ForestChatShortCutManager
import com.forest.forestchat.receiver.SmsDeliveredReceiver
import com.forest.forestchat.receiver.SmsSentReceiver
import com.forest.forestchat.utils.TelephonyThread
import com.klinker.android.send_message.SmsManagerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SendMessageFromNotificationUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncConversationsUseCase: SyncConversationsUseCase,
    private val syncMessagesUseCase: SyncMessagesUseCase,
    private val getConversationUseCase: GetConversationUseCase,
    private val updateConversationUseCase: UpdateConversationUseCase,
    private val getMessageByThreadIdUseCase: GetMessageByThreadIdUseCase,
    private val markAsFailedUseCase: MarkAsFailedUseCase,
    private val updateMessageUseCase: UpdateMessageUseCase,
    private val getOrCreateConversationUseCase: GetOrCreateConversationUseCase,
    private val shortCutManager: ForestChatShortCutManager
) {

    suspend operator fun invoke(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
    ) {
        if (addresses.isNotEmpty()) {
            // If a threadId isn't provided, try to obtain one
            var id = when (threadId) {
                0L -> TelephonyThread.getOrCreateThreadId(context, addresses.toSet())
                else -> threadId
            }

            sendMessage(subId, id, addresses, body)

            // Sync data if needed
            if (threadId == 0L) {
                syncConversationsUseCase()
            }

            id = when (threadId) {
                0L -> getOrCreateConversationUseCase(addresses)?.id ?: threadId
                else -> threadId
            }

            // Update Db
            getConversationUseCase(id)?.let { conversation ->
                getMessageByThreadIdUseCase(conversation.id)?.let { message ->
                    updateConversationUseCase(
                        conversation.copy(
                            lastMessage = message,
                            archived = false
                        )
                    )

                    shortCutManager.updateBadge()
                }
            }
        }
    }

    private suspend fun sendMessage(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
    ) {
        val message = convertToMessage(subId, threadId, addresses, body, System.currentTimeMillis())
        sendMessageToNativeProvider(message)

        if (threadId == 0L) {
            syncMessagesUseCase()
        }

        sendSms(message)
    }

    private fun convertToMessage(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
        date: Long
    ): Message =
        Message(
            threadId = threadId,
            contentId = 0L,
            address = addresses.first(),
            box = MessageBox.Outbox,
            type = MessageType.Sms,
            date = date,
            dateSent = 0L,
            read = true,
            seen = true,
            locked = false,
            subId = subId,
            sms = MessageSms(
                body = body,
                errorCode = 0,
                deliveryStatus = SmsStatus.None,
            ),
            mms = null,
        )

    private suspend fun sendMessageToNativeProvider(message: Message) {
        val values = contentValuesOf(
            Telephony.Sms.ADDRESS to message.address,
            Telephony.Sms.BODY to message.sms?.body,
            Telephony.Sms.DATE to System.currentTimeMillis(),
            Telephony.Sms.READ to true,
            Telephony.Sms.SEEN to true,
            Telephony.Sms.TYPE to Telephony.Sms.MESSAGE_TYPE_OUTBOX,
            Telephony.Sms.THREAD_ID to message.threadId,
            Telephony.Sms.SUBSCRIPTION_ID to message.subId
        )

        val uri = context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)

        uri?.lastPathSegment?.toLong()?.let { id ->
            updateMessageUseCase(message.copy(contentId = id))
        }
    }

    private suspend fun sendSms(message: Message) {
        val smsManager = message.subId.takeIf { it != -1 }
            ?.let(SmsManagerFactory::createSmsManager)
            ?: SmsManager.getDefault()

        val parts = when (val body = message.sms?.body) {
            null -> arrayListOf()
            else -> smsManager
                .divideMessage(body)
                ?: arrayListOf()
        }

        val sentIntents = parts.map {
            val intent = Intent(context, SmsSentReceiver::class.java).putExtra(
                SmsSentReceiver.MessageId,
                message.id
            )
            PendingIntent.getBroadcast(
                context,
                message.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val deliveredIntents = parts.map {
            val intent =
                Intent(context, SmsDeliveredReceiver::class.java).putExtra(
                    SmsDeliveredReceiver.MessageId,
                    message.id
                )
            PendingIntent.getBroadcast(
                context,
                message.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        try {
            smsManager.sendMultipartTextMessage(
                message.address,
                null,
                parts,
                ArrayList(sentIntents),
                ArrayList(deliveredIntents)
            )
        } catch (e: IllegalArgumentException) {
            markAsFailedUseCase(message.id, Telephony.MmsSms.ERR_TYPE_GENERIC)
        }
    }

}