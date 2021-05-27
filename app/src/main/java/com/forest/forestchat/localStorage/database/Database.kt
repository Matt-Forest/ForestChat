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
package com.forest.forestchat.localStorage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.domain.models.contact.Contact
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.localStorage.database.converters.*
import com.forest.forestchat.localStorage.database.daos.ContactDao
import com.forest.forestchat.localStorage.database.daos.ConversationDao
import com.forest.forestchat.localStorage.database.daos.MessageDao


@Database(
    entities = [
        Message::class,
        Contact::class,
        Conversation::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    value = [
        MessageConverters.MessageBoxConverter::class,
        MessageConverters.MessageTypeConverter::class,
        MessageConverters.MessageSmsConverter::class,
        MessageConverters.MessageMmsConverter::class,
        SmsConverters.SmsStatusConverter::class,
        MmsConverters.MmsPartTypeConverter::class,
        ContactConverters.PhoneNumberConverter::class,
        ConversationConverters.RecipientConverter::class,
        ConversationConverters.MessageBlockConverter::class,
        RecipientConverters.ContactConverter::class,
    ]
)
abstract class Database : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao
    abstract fun conversationDao(): ConversationDao
}