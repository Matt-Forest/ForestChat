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
package com.forest.forestchat.localStorage.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.domain.models.message.MessageType

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message): Long

    @Query("SELECT * FROM Message")
    suspend fun getAll(): List<Message>?

    @Query("SELECT * FROM Message WHERE threadId = :threadId")
    suspend fun getAllByThreadId(threadId: Long): List<Message>?

    @Query("SELECT * FROM Message WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Message?

    @Query("SELECT * FROM Message WHERE contentId = :contentId AND type = :type LIMIT 1")
    suspend fun getByContentId(contentId: Long, type: MessageType): Message?

    @Query("DELETE FROM Message WHERE threadId = :id")
    suspend fun deleteAllByThreadId(id: Long)

    @Query("DELETE FROM Message WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM Message")
    suspend fun deleteAll()

}