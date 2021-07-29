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
package com.forest.forestchat.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.transform.CircleCropTransformation
import com.forest.forestchat.R
import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.domain.useCases.GetConversationUseCase
import com.forest.forestchat.domain.useCases.GetMessageByIdUseCase
import com.forest.forestchat.domain.useCases.GetUnReadUnSeenMessagesUseCase
import com.forest.forestchat.extensions.asColor
import com.forest.forestchat.extensions.asString
import com.forest.forestchat.extensions.asStringArray
import com.forest.forestchat.extensions.dp
import com.forest.forestchat.receiver.MarkAsReadReceiver
import com.forest.forestchat.receiver.MarkAsSeenReceiver
import com.forest.forestchat.receiver.ReplyReceiver
import com.forest.forestchat.ui.NavigationActivity
import com.google.android.mms.ContentType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getUnReadUnSeenMessagesUseCase: GetUnReadUnSeenMessagesUseCase,
    private val getConversationUseCase: GetConversationUseCase,
    private val getMessageByIdUseCase: GetMessageByIdUseCase
) {

    companion object {
        const val DefaultNotificationChannel = "notifications_default"

        val VibratePattern = longArrayOf(0, 200, 0, 200)
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    suspend fun update(threadId: Long) {
        createNotificationChannel(threadId)
        val messages = getUnReadUnSeenMessagesUseCase(threadId)

        when (messages.isNullOrEmpty()) {
            true -> {
                notificationManager.cancel(threadId.toInt())
                notificationManager.cancel(threadId.toInt() + 100000)
                return
            }
            false -> {
                val conversation = getConversationUseCase(threadId) ?: return
                val date = conversation.lastMessage?.date ?: System.currentTimeMillis()

                val notification = buildNotification(threadId, date, messages)
                val avatar = buildAvatar(conversation)
                val messagingStyle = buildMessagingStyle(conversation, messages)

                notification
                    .setLargeIcon(avatar)
                    .setStyle(messagingStyle)

                // Add all of the people from this conversation to the notification, so that the system can
                // appropriately bypass DND mode
                conversation.recipients.forEach { recipient ->
                    notification.addPerson("tel:${recipient.address}")
                }

                notification.addAction(actionRead(threadId))
                notification.addAction(actionReply(threadId))

                notificationManager.notify(threadId.toInt(), notification.build())

                // Wake lock screen
                context.getSystemService<PowerManager>()?.let { powerManager ->
                    if (!powerManager.isInteractive) {
                        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context.packageName).apply {
                                acquire(5_000)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Create the NotificationChannel, but only on API 26+ because
     * the NotificationChannel class is new and not in the support library
     */
    suspend fun createNotificationChannel(threadId: Long = 0L) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getNotificationChannel(threadId) == null) {
            val channel = when (threadId) {
                0L -> NotificationChannel(
                    DefaultNotificationChannel,
                    "Default",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableLights(true)
                    lightColor = Color.WHITE
                    enableVibration(true)
                    vibrationPattern = VibratePattern
                }
                else -> {
                    val conversation = getConversationUseCase(threadId) ?: return
                    NotificationChannel(
                        buildNotificationChannelId(threadId),
                        conversation.getTitle(),
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        enableLights(true)
                        lightColor = Color.WHITE
                        enableVibration(true)
                        vibrationPattern = VibratePattern
                        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                        setSound(
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                        )
                    }
                }
            }
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotificationChannelId(threadId: Long): String {
        return when (threadId) {
            0L -> DefaultNotificationChannel
            else -> "notifications_$threadId"
        }
    }

    /**
     * Returns the channel id that should be used for a notification based on the threadId
     *
     * If a notification channel for the conversation exists, use the id for that. Otherwise return
     * the default channel id
     */
    private fun getChannelIdForNotification(threadId: Long): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return getNotificationChannel(threadId)?.id ?: DefaultNotificationChannel
        }

        return DefaultNotificationChannel
    }

    /**
     * Returns the notification channel for the given conversation, or null if it doesn't exist
     */
    private fun getNotificationChannel(threadId: Long): NotificationChannel? {
        val channelId = buildNotificationChannelId(threadId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return notificationManager.notificationChannels
                .find { channel -> channel.id == channelId }
        }

        return null
    }

    private fun buildNotification(
        threadId: Long,
        date: Long,
        messages: List<Message>
    ): NotificationCompat.Builder {
        // on click intent
        val intent = Intent(context, NavigationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(NavigationActivity.ThreadId, threadId)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        // on seen intent
        val seenIntent = Intent(context, MarkAsSeenReceiver::class.java).apply {
            putExtra(MarkAsSeenReceiver.ThreadId, threadId)
        }
        val seenPendingIntent = PendingIntent.getBroadcast(
            context,
            threadId.toInt(),
            seenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, getChannelIdForNotification(threadId))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setColor(R.color.primary.asColor(context))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSmallIcon(R.drawable.ic_conversation)
            .setNumber(messages.size)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(seenPendingIntent)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setLights(Color.WHITE, 500, 2_000)
            .setWhen(date)
            .setVibrate(VibratePattern)
    }

    private suspend fun buildAvatar(conversation: Conversation): Bitmap? =
        conversation.recipients.takeIf { it.size == 1 }
            ?.first()?.contact?.photoUri
            ?.let { photoUri -> buildIconBitmap(photoUri) }

    private suspend fun buildMessagingStyle(
        conversation: Conversation,
        messages: List<Message>
    ): NotificationCompat.Style {
        val user = Person.Builder().setName("Me").build()
        val messagingStyle = NotificationCompat.MessagingStyle(user)

        // Tell the notification if it's a group message
        if (conversation.recipients.size >= 2) {
            messagingStyle.isGroupConversation = true
            messagingStyle.conversationTitle = conversation.getTitle()
        }

        // Add the messages to the notification
        messages.forEach { message ->
            val person = Person.Builder()

            if (!message.isUser()) {
                val recipient = conversation.recipients.find { recipient ->
                    PhoneNumberUtils.compare(recipient.address, message.address)
                }

                person.setName(recipient?.getDisplayName() ?: message.address)
                person.setIcon(
                    buildIconBitmap(recipient?.contact?.photoUri)
                        ?.let(IconCompat::createWithBitmap)
                )

                recipient?.contact
                    ?.let { contact -> "${ContactsContract.Contacts.CONTENT_LOOKUP_URI}/${contact.lookupKey}" }
                    ?.let(person::setUri)
            }

            NotificationCompat.MessagingStyle.Message(
                message.getSummary(),
                message.date,
                person.build()
            ).apply {
                message.mms?.parts?.firstOrNull { ContentType.isImageType(it.type) }?.let { part ->
                    setData(
                        part.type,
                        ContentUris.withAppendedId(Uri.parse("content://mms/part"), part.id)
                    )
                }
                messagingStyle.addMessage(this)
            }
        }

        return messagingStyle
    }

    private suspend fun buildIconBitmap(uri: String?): Bitmap? {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(uri)
            .size(64.dp, 64.dp)
            .transformations(CircleCropTransformation())
            .allowHardware(false) // Disable hardware bitmaps.
            .build()

        return when (val res = loader.execute(request)) {
            is SuccessResult -> (res.drawable as BitmapDrawable).bitmap
            is ErrorResult -> null
        }
    }

    private fun actionRead(threadId: Long): NotificationCompat.Action? {
        val intent = Intent(context, MarkAsReadReceiver::class.java).putExtra(
            MarkAsReadReceiver.ThreadId,
            threadId
        )
        val pi = PendingIntent.getBroadcast(
            context, threadId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Action.Builder(
            R.drawable.ic_check,
            R.string.notification_mark_read.asString(context),
            pi
        )
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ).build()
    }

    private fun actionReply(threadId: Long): NotificationCompat.Action? {
        return when (Build.VERSION.SDK_INT >= 24) {
            true -> getReplyAction(threadId)
            false -> {
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    threadId.toInt(),
                    Intent(context, NavigationActivity::class.java).putExtra(
                        NavigationActivity.ConversationThreadId,
                        threadId
                    ),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                NotificationCompat.Action
                    .Builder(
                        R.drawable.ic_reply,
                        R.string.notification_reply.asString(context),
                        pendingIntent
                    )
                    .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                    .build()
            }
        }
    }

    private fun getReplyAction(threadId: Long): NotificationCompat.Action {
        val replyIntent = Intent(context, ReplyReceiver::class.java)
            .putExtra(ReplyReceiver.ThreadId, threadId)
        val replyPI = PendingIntent.getBroadcast(
            context, threadId.toInt(), replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = R.string.notification_reply.asString(context)
        val responseSet = R.array.notification_forest_chat_responses.asStringArray(context)
        val remoteInput = RemoteInput.Builder("body")
            .setLabel(title)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            remoteInput.setChoices(responseSet)
        }

        return NotificationCompat.Action.Builder(R.drawable.ic_reply, title, replyPI)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .addRemoteInput(remoteInput.build())
            .build()
    }

    suspend fun notifyFailed(messageId: Long) {
        getMessageByIdUseCase(messageId)?.let { message ->
            if (!message.isFailed()) {
                getConversationUseCase(message.threadId)?.let { conversation ->
                    val threadId = conversation.id

                    val intent = Intent(
                        context,
                        NavigationActivity::class.java
                    ).putExtra(NavigationActivity.ThreadId, threadId)
                    val contentPi = PendingIntent.getActivity(context, 0, intent, 0)

                    val notification =
                        NotificationCompat.Builder(context, getChannelIdForNotification(threadId))
                            .setContentTitle(context.getString(R.string.notification_message_failed_title))
                            .setContentText(
                                context.getString(
                                    R.string.notification_message_failed_text,
                                    conversation.getTitle()
                                )
                            )
                            .setColor(R.color.primary.asColor(context))
                            .setPriority(NotificationManagerCompat.IMPORTANCE_MAX)
                            .setSmallIcon(R.drawable.ic_conversation_failed)
                            .setAutoCancel(true)
                            .setContentIntent(contentPi)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setLights(Color.WHITE, 500, 2_000)
                            .setVibrate(VibratePattern)

                    notificationManager.notify(threadId.toInt() + 100_000, notification.build())
                }
            }
        }
    }

}