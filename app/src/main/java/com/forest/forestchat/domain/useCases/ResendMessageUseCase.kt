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
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.domain.models.message.MessageType
import com.forest.forestchat.receiver.SmsDeliveredReceiver
import com.forest.forestchat.receiver.SmsSentReceiver
import com.forest.forestchat.utils.*
import com.google.android.mms.pdu_alt.MultimediaMessagePdu
import com.google.android.mms.pdu_alt.PduPersister
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.SmsManagerFactory
import com.klinker.android.send_message.Transaction
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ResendMessageUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val markAsSendingUseCase: MarkAsSendingUseCase,
    private val getMessageByIdUseCase: GetMessageByIdUseCase,
    private val markAsFailedUseCase: MarkAsFailedUseCase,
) {

    suspend operator fun invoke(messageId: Long) {
        markAsSendingUseCase(messageId)
        getMessageByIdUseCase(messageId)?.let { message ->
            when (message.type) {
                MessageType.Sms -> sendSms(message)
                MessageType.Mms -> sendMms(message)
                else -> null
            }
        }
    }

    private suspend fun sendSms(message: Message) {
        val smsManager = message.subId.takeIf { it != -1 }
            ?.let(SmsManagerFactory::createSmsManager)
            ?: SmsManager.getDefault()

        val parts = message.sms?.body?.let { smsManager.divideMessage(it) } ?: arrayListOf()

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

    private fun sendMms(message: Message) {
        val pdu = tryOrNull {
            PduPersister.getPduPersister(context).load(message.getUri()) as MultimediaMessagePdu
        } ?: return

        val addresses = pdu.to.map { it.string }.filter { it.isNotBlank() }

        val settings = Settings()
        settings.useSystemSending = true
        settings.deliveryReports = true
        message.subId?.let { settings.subscriptionId = it }

        val transaction = Transaction(context, settings)
        val recipients = addresses.map(PhoneNumberUtils::normalizeNumber)
        val messageMms = com.klinker.android.send_message.Message("", recipients.toTypedArray())

        message.mms?.parts?.forEach { part ->
            val byte = tryOrNull {
                context.contentResolver.openInputStream(part.getUri())
                    ?.use { inputStream -> inputStream.readBytes() }
            } ?: return@forEach

            messageMms.addMedia(byte, part.type, part.name)
        }

        transaction.sendNewMessage(messageMms, message.threadId)
    }

}