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
package com.forest.forestchat.receiver

import android.app.Activity
import android.content.*
import android.net.Uri
import android.provider.Telephony
import com.forest.forestchat.app.TransversalBusEvent
import com.forest.forestchat.domain.useCases.GetConversationUseCase
import com.forest.forestchat.domain.useCases.SyncMessageFromUriUseCase
import com.forest.forestchat.domain.useCases.UpdateLastMessageConversationUseCase
import com.forest.forestchat.manager.ForestChatShortCutManager
import com.forest.forestchat.manager.NotificationManager
import com.google.android.mms.MmsException
import com.google.android.mms.util_alt.SqliteWrapper
import com.klinker.android.send_message.MmsSentReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

/**
 * We need to pass by a BroadcastReceiver instead of MmsReceivedReceiver from klinker because,
 * Dagger need the onReceiver for the injection.
 */
@AndroidEntryPoint
class MmsSentReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var forestChatShortCutManager: ForestChatShortCutManager

    @Inject
    lateinit var syncMessageFromUriUseCase: SyncMessageFromUriUseCase

    @Inject
    lateinit var getConversationUseCase: GetConversationUseCase

    @Inject
    lateinit var updateLastMessageConversationUseCase: UpdateLastMessageConversationUseCase


    override fun onReceive(context: Context?, intent: Intent?) {
        Receiver(object : Receiver.MmsSentListener {

            override fun onMessageStatusUpdated(statusIntent: Intent?, resultCode: Int) {
                statusIntent?.getStringExtra("content_uri")?.let { uriString ->
                    val uri = Uri.parse(uriString)

                    when (resultCode) {
                        Activity.RESULT_OK -> {
                            val values = ContentValues(1)
                            values.put(Telephony.Mms.MESSAGE_BOX, Telephony.Mms.MESSAGE_BOX_SENT)
                            context?.let {
                                SqliteWrapper.update(
                                    it,
                                    it.contentResolver,
                                    uri,
                                    values,
                                    null,
                                    null
                                )
                            }
                        }
                        else -> {
                            try {
                                val messageId = ContentUris.parseId(uri)

                                val values = ContentValues(1)
                                values.put(
                                    Telephony.Mms.MESSAGE_BOX,
                                    Telephony.Mms.MESSAGE_BOX_FAILED
                                )
                                context?.let {
                                    SqliteWrapper.update(
                                        it, it.contentResolver, Telephony.Mms.CONTENT_URI, values,
                                        "${Telephony.Mms._ID} = ?", arrayOf(messageId.toString())
                                    )
                                }

                                // Need to figure out why the message isn't appearing in the PendingMessages Uri,
                                // so that we can properly assign the error type
                                val errorTypeValues = ContentValues(1)
                                errorTypeValues.put(
                                    Telephony.MmsSms.PendingMessages.ERROR_TYPE,
                                    Telephony.MmsSms.ERR_TYPE_GENERIC_PERMANENT
                                )
                                context?.let {
                                    SqliteWrapper.update(
                                        it,
                                        it.contentResolver,
                                        Telephony.MmsSms.PendingMessages.CONTENT_URI,
                                        errorTypeValues,
                                        "${Telephony.MmsSms.PendingMessages.MSG_ID} = ?",
                                        arrayOf(messageId.toString())
                                    )
                                }
                            } catch (e: MmsException) {
                                e.printStackTrace()
                            }
                        }
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        syncMessageFromUriUseCase(uri)?.let { message ->
                            getConversationUseCase(message.threadId)?.let { conversation ->
                                updateLastMessageConversationUseCase(conversation)
                                notificationManager.update(conversation.id)
                            }
                        }

                        forestChatShortCutManager.updateShortcuts()
                        forestChatShortCutManager.updateBadge()
                        EventBus.getDefault().post(TransversalBusEvent.RefreshMessages)
                    }
                }
            }

        }).onReceive(context, intent)
    }

    private class Receiver(private val listener: MmsSentListener) : MmsSentReceiver() {

        override fun onMessageStatusUpdated(context: Context?, intent: Intent?, resultCode: Int) {
            super.onMessageStatusUpdated(context, intent, resultCode)
            listener.onMessageStatusUpdated(intent, resultCode)
        }

        interface MmsSentListener {
            fun onMessageStatusUpdated(statusIntent: Intent?, resultCode: Int)
        }

    }

}