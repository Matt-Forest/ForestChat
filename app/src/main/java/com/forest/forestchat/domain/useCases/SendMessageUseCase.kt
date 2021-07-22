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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import androidx.core.content.contentValuesOf
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.PixelSize
import com.bumptech.glide.Glide
import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.domain.models.message.MessageBox
import com.forest.forestchat.domain.models.message.MessageType
import com.forest.forestchat.domain.models.message.sms.MessageSms
import com.forest.forestchat.domain.models.message.sms.SmsStatus
import com.forest.forestchat.manager.ForestChatShortCutManager
import com.forest.forestchat.receiver.SmsDeliveredReceiver
import com.forest.forestchat.receiver.SmsSentReceiver
import com.forest.forestchat.ui.common.glide.GifEncoder
import com.forest.forestchat.ui.conversation.models.Attachment
import com.forest.forestchat.utils.*
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.SmsManagerFactory
import com.klinker.android.send_message.Transaction
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt


@Singleton
class SendMessageUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getConversationUseCase: GetConversationUseCase,
    private val updateConversationUseCase: UpdateConversationUseCase,
    private val getMessageByThreadIdUseCase: GetMessageByThreadIdUseCase,
    private val markAsFailedUseCase: MarkAsFailedUseCase,
    private val updateMessageUseCase: UpdateMessageUseCase,
    private val getOrCreateConversationByAddressesUseCase: GetOrCreateConversationByAddressesUseCase,
    private val syncMessageFromUriUseCase: SyncMessageFromUriUseCase,
    private val shortCutManager: ForestChatShortCutManager
) {

    companion object {
        const val MmsMaxSize = 300
    }

    suspend operator fun invoke(
        subId: Int,
        conversation: Conversation,
        body: String,
        attachments: List<Attachment>
    ) {
        val addresses = conversation.recipients.map { it.address }
        when {
            addresses.size == 1 || conversation.grouped -> {
                // sending to one person or sending to a group of people
                this(subId, conversation.id, addresses, body, attachments)
            }
            else -> {
                // sending to a diffusion list
                addresses.forEach { address ->
                    val threadId = tryOrNull {
                        TelephonyThread.getOrCreateThreadId(context, listOf(address))
                    } ?: 0
                    val sendAddress = listOf(
                        getConversationUseCase(threadId)
                            ?.recipients
                            ?.firstOrNull()
                            ?.address
                            ?: address
                    )
                    this(subId, threadId, sendAddress, body, attachments)
                }
            }
        }
    }

    suspend operator fun invoke(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
        attachments: List<Attachment>
    ) {
        if (addresses.isNotEmpty()) {
            // If a threadId isn't provided, try to obtain one
            val id = when (threadId) {
                0L -> TelephonyThread.getOrCreateThreadId(context, addresses.toSet())
                else -> threadId
            }

            sendMessage(subId, id, addresses, body, attachments)

            val newThreadId = when (threadId) {
                0L -> getOrCreateConversationByAddressesUseCase(addresses)?.id ?: threadId
                else -> threadId
            }

            // Update Db
            getConversationUseCase(newThreadId)?.let { conversation ->
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
        attachments: List<Attachment>
    ) {
        val smsManager = subId.takeIf { it != -1 }
            ?.let(SmsManagerFactory::createSmsManager)
            ?: SmsManager.getDefault()

        val parts = smsManager.divideMessage(body) ?: arrayListOf()
        val forceMms = parts.size > 1

        if (addresses.size == 1 && attachments.isEmpty() && !forceMms) {
            /* ---- SMS ---- */
            val message = sendMessageToNativeProvider(
                convertToMessage(
                    subId,
                    threadId,
                    addresses,
                    body,
                    System.currentTimeMillis()
                )
            )

            sendSms(message, parts, smsManager)
        } else {
            /* ---- MMS ---- */
            sendMms(subId, threadId, addresses, body, attachments)
        }
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
            subId = subId,
            sms = MessageSms(
                body = body,
                errorCode = 0,
                deliveryStatus = SmsStatus.None,
            ),
            mms = null,
        )

    private suspend fun sendMessageToNativeProvider(message: Message): Message {
        var messageReturn = message
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

        // We do this after inserting the message because it might be slow, and we want the message
        // to be inserted into Db immediately. We don't need to do this after receiving one
        uri?.lastPathSegment?.toLong()?.let { contentId ->
            val id = updateMessageUseCase(message.copy(contentId = contentId))
            messageReturn = message.copy(id = id)
        }

        // On some devices, we can't obtain a threadId until after the first message is sent in a
        // conversation. In this case, we need to update the message's threadId after it gets added
        // to the native ContentProvider
        if (message.threadId == 0L) {
            uri?.let { syncMessageFromUriUseCase(it) }
        }

        return messageReturn
    }

    private suspend fun sendSms(
        message: Message,
        parts: ArrayList<String>,
        smsManager: SmsManager
    ) {
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

    private suspend fun sendMms(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
        attachments: List<Attachment>
    ) {
        val maxWidth = Int.MAX_VALUE

        val maxHeight = Int.MAX_VALUE

        // 0.9 --> buys us a bit of wiggle room
        var remainingBytes = (MmsMaxSize * 1024) * 0.9
        remainingBytes -= body.toByteArray().size

        val settings = Settings()
        settings.useSystemSending = true
        settings.deliveryReports = true
        if (subId != -1) {
            settings.subscriptionId = subId
        }

        val transaction = Transaction(context, settings)
        val recipients = addresses.map(PhoneNumberUtils::normalizeNumber)
        val message = com.klinker.android.send_message.Message(body, recipients.toTypedArray())

        attachments.mapNotNull { attachment -> attachment as? Attachment.Contact }
            .map { attachment -> attachment.vCard.toByteArray() }
            .forEach { vCard ->
                remainingBytes -= vCard.size
                message.addMedia(vCard, MimeTypeContactCard, "contact")
            }

        val imageBytesByAttachment = attachments
            .mapNotNull { attachment -> attachment as? Attachment.Image }
            .associateWith { attachment ->
                val uri = attachment.getUri() ?: return@associateWith byteArrayOf()
                when (attachment.isGif(context)) {
                    true -> reduceGif(uri, maxWidth, maxHeight)
                    false -> reduceImage(uri, null)
                }
            }
            .toMutableMap()

        val imageByteCount = imageBytesByAttachment.values.sumOf { byteArray -> byteArray.size }
        if (imageByteCount > remainingBytes) {
            imageBytesByAttachment.forEach { (attachment, originalBytes) ->
                val uri = attachment.getUri() ?: return@forEach
                val maxBytes = originalBytes.size / imageByteCount.toFloat() * remainingBytes

                // Get the image dimensions
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(
                    context.contentResolver.openInputStream(uri),
                    null,
                    options
                )
                val width = options.outWidth
                val height = options.outHeight
                val aspectRatio = width.toFloat() / height.toFloat()

                var attempts = 0
                var scaledBytes = originalBytes

                while (scaledBytes.size > maxBytes) {
                    // Estimate how much we need to scale the image down by. If it's still too big, we'll need to
                    // try smaller and smaller values
                    val scale = maxBytes / originalBytes.size * (0.9 - attempts * 0.2)
                    if (scale <= 0) {
                        // fail to compress
                        return@forEach
                    }

                    val newArea = scale * width * height
                    val newWidth = sqrt(newArea * aspectRatio).toInt()
                    val newHeight = (newWidth / aspectRatio).toInt()

                    attempts++
                    scaledBytes = when (attachment.isGif(context)) {
                        true -> reduceGif(uri, newWidth, newHeight)
                        false -> reduceImage(uri, PixelSize(newWidth, newHeight))
                    }
                }

                imageBytesByAttachment[attachment] = scaledBytes
            }
        }

        imageBytesByAttachment.forEach { (attachment, bytes) ->
            when (attachment.isGif(context)) {
                true -> message.addMedia(
                    bytes,
                    MimeTypeGif,
                    "gif"
                )
                false -> message.addMedia(
                    bytes,
                    MimeTypeJpeg,
                    "image"
                )
            }
        }

        transaction.sendNewMessage(message, threadId)
    }

    private suspend fun reduceImage(uri: Uri, size: PixelSize?): ByteArray {
        val request = ImageRequest.Builder(context).apply {
            data(uri)
            size?.let { size(it) }
        }.build()

        val drawable = context.imageLoader.execute(request).drawable
        return convertBitmapToByteArray((drawable as BitmapDrawable).bitmap)
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    private fun reduceGif(uri: Uri, maxWidth: Int, maxHeight: Int): ByteArray {
        val gif = Glide
            .with(context)
            .asGif()
            .load(uri)
            .centerInside()
            .encodeQuality(90)
            .submit(maxWidth, maxHeight)
            .get()

        val outputStream = ByteArrayOutputStream()
        GifEncoder(context, Glide.get(context).bitmapPool)
            .encodeTransformedToStream(gif, outputStream)
        return outputStream.toByteArray()
    }

}