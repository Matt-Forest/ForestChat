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
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MessageMms(
    val subject: String?,
    val parts: List<MmsPart>,
    val errorCode: Int
) : Parcelable {

    fun getSummary() : String {
        val sb = StringBuilder()

        // Add subject
        getCleansedSubject().takeIf { it.isNotEmpty() }?.let { subject -> sb.appendLine(subject) }

        // Add parts
        parts.mapNotNull { it.getSummary() }.forEach { summary -> sb.appendLine(summary) }

        return sb.toString().trim()
    }

    /**
     * Cleanses the subject in case it's useless, so that the UI doesn't have to show it
     */
    private fun getCleansedSubject(): String {
        val uselessSubjects = listOf("no subject", "NoSubject", "<not present>")

        return if (uselessSubjects.contains(subject)) "" else subject ?: ""
    }

}
