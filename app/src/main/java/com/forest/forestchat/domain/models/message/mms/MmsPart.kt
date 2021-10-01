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
package com.forest.forestchat.domain.models.message.mms

import android.os.Parcelable
import androidx.core.net.toUri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.forest.forestchat.utils.MimeTypeContactCard
import com.forest.forestchat.utils.MimeTypeGif
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class MmsPart(
    @PrimaryKey
    val id: Long,
    val messageId: Long,
    val type: String,
    val seq: Int,
    val name: String?,
    val text: String?
) : Parcelable {

    fun getSummary(): String? = when {
        isText() -> text
        isContactCard() -> "Contact card"
        isImage() -> "Photo"
        isVideo() -> "Video"
        else -> null
    }

    fun getUri() = "content://mms/part/$id".toUri()

    fun isText(): Boolean = type == "text/plain"

    fun isVideo(): Boolean = type.startsWith("video")

    fun isGif(): Boolean = type == MimeTypeGif

    fun isImage(): Boolean = type.startsWith("image")

    fun isContactCard(): Boolean = type == MimeTypeContactCard

    fun isMedia(): Boolean = isImage() || isVideo()

    fun isSmil(): Boolean = type == "application/smil"

}
