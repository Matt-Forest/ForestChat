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
package com.forest.forestchat.ui.conversations.adapter

sealed class HomeConversationsPayload {
    data class Pin(val pin: Boolean) : HomeConversationsPayload()
    data class Title(val newTitle: String) : HomeConversationsPayload()
    data class NewLastMessage(val newLastMessage: String) : HomeConversationsPayload()
    data class UpdateMessageAndMarkAsRead(val isRead: Boolean, val newLastMessage: String) : HomeConversationsPayload()
    data class MarkAsRead(val isRead: Boolean) : HomeConversationsPayload()
}
