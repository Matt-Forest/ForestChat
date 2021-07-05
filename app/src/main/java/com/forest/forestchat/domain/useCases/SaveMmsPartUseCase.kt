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
package com.forest.forestchat.domain.useCases

import android.content.Context
import android.media.MediaScannerConnection
import android.webkit.MimeTypeMap
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.domain.models.message.mms.MmsPart
import com.forest.forestchat.localStorage.database.daos.MessageDao
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveMmsPartUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageDao: MessageDao
) {

    suspend operator fun invoke(part: MmsPart): File? {
        val message = messageDao.getById(part.messageId)

        return createFile(part, message)
    }

    private fun createFile(mmsPart: MmsPart, message: Message?): File? {
        val extension =
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mmsPart.type) ?: return null
        val date = message?.date
        val dir = context.getExternalFilesDir("ForestChat/Media")?.apply { mkdirs() }
        val fileName = mmsPart.name?.takeIf { name -> name.endsWith(extension) }
            ?: "${mmsPart.type.split("/").last()}_$date.$extension"
        var file: File
        var index = 0
        do {
            file = File(
                dir,
                if (index == 0) fileName else fileName.replace(
                    ".$extension",
                    " ($index).$extension"
                )
            )
            index++
        } while (file.exists())

        try {
            FileOutputStream(file).use { outputStream ->
                context.contentResolver.openInputStream(mmsPart.getUri())?.use { inputStream ->
                    inputStream.copyTo(outputStream, 1024)
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        MediaScannerConnection.scanFile(context, arrayOf(file.path), null, null)

        return file.takeIf { it.exists() }
    }

}