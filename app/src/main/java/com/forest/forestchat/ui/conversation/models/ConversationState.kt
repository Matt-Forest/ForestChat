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

import android.telephony.SubscriptionInfo
import com.forest.forestchat.domain.models.Recipient
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.ui.common.avatar.AvatarType

sealed class ConversationState {

    data class Data(
        val messages: List<Message>,
        val recipients: List<Recipient>,
        val subscriptionsInfo: List<SubscriptionInfo>
    ) : ConversationState()

    data class Empty(
        val avatarType: AvatarType,
        val title: String,
        val phone: String?
    ) : ConversationState()

}
