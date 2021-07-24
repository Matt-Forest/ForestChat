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
package com.forest.forestchat.ui.settingsConversation.models

import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.ui.common.media.Media

sealed class SettingsConversationEvent {

    data class ShowTitleUpdate(val title: String) : SettingsConversationEvent()

    data class ShowContact(
        val lookupKey: String?,
        val address: String?
    ) : SettingsConversationEvent()

    data class ShowGallery(
        val mediaSelected: Media,
        val medias: List<Media>
    ) : SettingsConversationEvent()

    data class ShowNotification(val conversation: Conversation) : SettingsConversationEvent()

}
