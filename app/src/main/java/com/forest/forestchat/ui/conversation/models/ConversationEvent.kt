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
package com.forest.forestchat.ui.conversation.models

import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.ui.common.media.Media
import java.io.File

sealed class ConversationEvent {

    object RequestDefaultSms : ConversationEvent()
    object RequestSmsPermission : ConversationEvent()
    object RequestStoragePermission : ConversationEvent()
    object RequestCamera : ConversationEvent()
    object RequestGallery : ConversationEvent()
    object RequestContact : ConversationEvent()

    data class ShowFile(val file: File) : ConversationEvent()
    data class ShowMessageOptions(val canCopy: Boolean, val canResend: Boolean) : ConversationEvent()
    data class ShowMessageDetails(val details: String) : ConversationEvent()
    data class ShowGallery(val mediaSelected: Media, val medias: List<Media>) : ConversationEvent()
    data class Call(val address: String, val asPermissionToCall: Boolean) : ConversationEvent()
    data class GoToSettings(val conversation: Conversation) : ConversationEvent()

}
