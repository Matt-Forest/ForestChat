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
package com.forest.forestchat.localStorage.database.converters

import androidx.room.TypeConverter
import com.forest.forestchat.domain.models.message.MessageBox
import com.forest.forestchat.domain.models.message.MessageType
import com.forest.forestchat.domain.models.message.mms.MessageMms
import com.forest.forestchat.domain.models.message.sms.MessageSms
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MessageConverters {

    class MessageBoxConverter {

        @TypeConverter
        fun typeToInt(type: MessageBox?): Int? = type?.code

        @TypeConverter
        fun typeFromInt(code: Int?): MessageBox? = MessageBox.values().find { it.code == code }

    }

    class MessageTypeConverter {

        @TypeConverter
        fun typeToInt(type: MessageType?): Int? = type?.code

        @TypeConverter
        fun typeFromInt(code: Int?): MessageType? = MessageType.values().find { it.code == code }

    }

    class MessageSmsConverter {

        private val adapter : JsonAdapter<MessageSms> =
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
                .adapter(MessageSms::class.java)

        @TypeConverter
        fun messageSmsToString(sms: MessageSms?): String? = sms?.let {
            adapter.toJson(sms)
        }

        @TypeConverter
        fun messageSmsFromString(json: String?): MessageSms? = json?.let {
            adapter.fromJson(json)
        }

    }

    class MessageMmsConverter {

        private val adapter : JsonAdapter<MessageMms> =
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
                .adapter(MessageMms::class.java)

        @TypeConverter
        fun messageMmsToString(sms: MessageMms?): String? = sms?.let {
            adapter.toJson(sms)
        }

        @TypeConverter
        fun messageMmsFromString(json: String?): MessageMms? = json?.let {
            adapter.fromJson(json)
        }

    }

}