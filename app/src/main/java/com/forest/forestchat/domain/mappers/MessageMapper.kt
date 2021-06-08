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
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.domain.models.message.MessageBox
import com.forest.forestchat.domain.models.message.MessageType
import com.forest.forestchat.domain.models.message.mms.MmsPart
import com.google.android.mms.pdu_alt.PduHeaders

fun Cursor.toMessage(context: Context): Message {
    val type: MessageType = when {
        getColumnIndex(Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN) != -1 -> getString(
            getColumnIndex(Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN)
        )
        getColumnIndex(Telephony.Mms.SUBJECT) != -1 -> "mms"
        getColumnIndex(Telephony.Sms.ADDRESS) != -1 -> "sms"
        else -> null
    }.toMessageType()
    val contentId = getLong(getColumnIndex(Telephony.MmsSms._ID))

    return Message(
        threadId = getLong(getColumnIndex(Telephony.Mms.THREAD_ID)),
        contentId = contentId,
        address = when (type) {
            MessageType.Sms -> getString(getColumnIndex(Telephony.Sms.ADDRESS))
            MessageType.Mms -> getMmsAddress(context, contentId)
            else -> null
        },
        box = MessageBox.values().find {
            it.code == when (type) {
                MessageType.Sms -> getInt(getColumnIndex(Telephony.Sms.TYPE))
                MessageType.Mms -> getInt(getColumnIndex(Telephony.Mms.MESSAGE_BOX))
                else -> 0
            }
        } ?: MessageBox.All,
        type = type,
        date = getLong(getColumnIndex(Telephony.Mms.DATE)).toDate(type),
        dateSent = getLong(getColumnIndex(Telephony.Mms.DATE_SENT)).toDate(type),
        read = getInt(getColumnIndex(Telephony.Mms.READ)) != 0,
        seen = getInt(getColumnIndex(Telephony.Mms.SEEN)) != 0,
        locked = when (type) {
            MessageType.Sms -> getInt(getColumnIndex(Telephony.Sms.LOCKED)) != 0
            MessageType.Mms -> getInt(getColumnIndex(Telephony.Mms.LOCKED)) != 0
            else -> false
        },
        subId = when (type) {
            MessageType.Sms -> when (getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID) != -1) {
                true -> getInt(getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID))
                false -> null
            }
            MessageType.Mms -> when (getColumnIndex(Telephony.Mms.SUBSCRIPTION_ID) != -1) {
                true -> getInt(getColumnIndex(Telephony.Mms.SUBSCRIPTION_ID))
                false -> null
            }
            else -> null
        },
        sms = when (type) {
            MessageType.Sms -> toSms()
            else -> null
        },
        mms = when (type) {
            MessageType.Mms -> toMms(getMmsPartByMessageId(context, contentId))
            else -> null
        }
    )
}

private fun Long.toDate(type: MessageType): Long = when (type) {
    MessageType.Mms -> this * 1_000
    else -> this
}

private fun String?.toMessageType(): MessageType = when {
    this == "sms" -> MessageType.Sms
    this == "mms" -> MessageType.Mms
    else -> MessageType.Unknown
}

private fun getMmsAddress(context: Context, messageId: Long): String? {
    val uri = Telephony.Mms.CONTENT_URI.buildUpon()
        .appendPath(messageId.toString())
        .appendPath("addr").build()

    val projection = arrayOf(Telephony.Mms.Addr.ADDRESS, Telephony.Mms.Addr.CHARSET)
    val selection = "${Telephony.Mms.Addr.TYPE} = ${PduHeaders.FROM}"

    context.contentResolver.query(uri, projection, selection, null, null)
        ?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }

    return null
}

private fun getMmsPartByMessageId(context: Context, messageId: Long): List<MmsPart> {
    val uri = Uri.parse("content://mms/part")
    val projection = "${Telephony.Mms.Part.MSG_ID} = ?"
    val selection = arrayOf(messageId.toString())

    val result = mutableListOf<MmsPart>()

    context.contentResolver.query(
        uri, null, projection, selection, null
    )?.use { cursor ->
        while (cursor.moveToNext()) {
            result.add(cursor.toMmsPart())
        }
    }

    return result
}