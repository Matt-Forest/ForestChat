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
package com.forest.forestchat.utils

import android.content.Context
import com.forest.forestchat.R
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.domain.models.message.MessageType
import com.forest.forestchat.extensions.getDetailedTimestamp
import com.google.android.mms.pdu_alt.EncodedStringValue
import com.google.android.mms.pdu_alt.MultimediaMessagePdu
import com.google.android.mms.pdu_alt.PduPersister
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageDetailsFormatter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    operator fun invoke(message: Message): String {
        val builder = StringBuilder()

        message.type
            .takeIf { it != MessageType.Unknown }
            ?.name
            ?.let { context.getString(R.string.message_details_type, it) }
            ?.let(builder::appendLine)

        when (message.type) {
            MessageType.Sms -> {
                message.address
                    .takeIf { it?.isNotBlank() == true && !message.isUser() }
                    ?.let { context.getString(R.string.message_details_from, it) }
                    ?.let(builder::appendLine)

                message.address
                    .takeIf { it?.isNotBlank() == true && message.isUser() }
                    ?.let { context.getString(R.string.message_details_to, it) }
                    ?.let(builder::appendLine)
            }
            MessageType.Mms -> {
                val pdu = tryOrNull {
                    PduPersister.getPduPersister(context)
                        .load(message.getUri())
                            as MultimediaMessagePdu
                }

                pdu?.from?.string
                    ?.takeIf { it.isNotBlank() }
                    ?.let { context.getString(R.string.message_details_from, it) }
                    ?.let(builder::appendLine)

                pdu?.to
                    ?.let(EncodedStringValue::concat)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { context.getString(R.string.message_details_to, it) }
                    ?.let(builder::appendLine)
            }
            else -> null
        }

        message.date
            .takeIf { it > 0 && message.isUser() }
            ?.getDetailedTimestamp(context)
            ?.let { context.getString(R.string.message_details_sent, it) }
            ?.let(builder::appendLine)

        message.dateSent
            .takeIf { it > 0 && !message.isUser() }
            ?.getDetailedTimestamp(context)
            ?.let { context.getString(R.string.message_details_sent, it) }
            ?.let(builder::appendLine)

        message.date
            .takeIf { it > 0 && !message.isUser() }
            ?.getDetailedTimestamp(context)
            ?.let { context.getString(R.string.message_details_received, it) }
            ?.let(builder::appendLine)

        message.dateSent
            .takeIf { it > 0 && message.isUser() }
            ?.getDetailedTimestamp(context)
            ?.let { context.getString(R.string.message_details_delivered, it) }
            ?.let(builder::appendLine)

        if (message.type == MessageType.Sms) {
            message.sms?.errorCode
                .takeIf { it != 0 }
                ?.let { context.getString(R.string.message_details_error_code, it) }
                ?.let(builder::appendLine)
        }

        return builder.toString().trim()
    }

}