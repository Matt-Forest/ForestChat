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
package com.forest.forestchat.ui.common.mappers

import com.forest.forestchat.domain.models.Recipient
import com.forest.forestchat.domain.models.contact.Contact
import com.forest.forestchat.ui.common.avatar.AvatarType


fun buildAvatar(recipients: List<Recipient>): AvatarType = when {
    recipients.size == 1 -> buildSingleAvatar(recipients[0].contact, false)
    recipients.size > 1 -> AvatarType.Group(
        buildSingleAvatar(recipients[0].contact, true),
        buildSingleAvatar(recipients[1].contact, true)
    )
    else -> AvatarType.Single.Profile
}

fun buildSingleAvatar(contact: Contact?, isFromGroup: Boolean): AvatarType.Single =
    when {
        contact?.photoUri?.isNotBlank() == true ->
            AvatarType.Single.Image(contact.photoUri)
        contact?.name?.isNotBlank() == true ->
            AvatarType.Single.Letters(buildInitial(contact.name, isFromGroup))
        else -> AvatarType.Single.Profile
    }

private fun buildInitial(name: String, isFromGroup: Boolean): String {
    val initials = name.substringBefore(',')
        .split(" ")
        .filter { it.isNotEmpty() }
        .map { it[0] }
        .filter { initial -> initial.isLetterOrDigit() }
        .map { it.toString() }

    return when {
        isFromGroup -> initials.first()
        initials.size > 1 -> initials.first() + initials.last()
        else -> initials.first()
    }
}