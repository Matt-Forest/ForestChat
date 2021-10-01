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
package com.forest.forestchat.extensions

import android.net.Uri

sealed class ImageSignatureKeys {

    sealed class Conversation : ImageSignatureKeys() {
        object Gallery : Conversation()
        object Message : Conversation()
    }

    object AttachmentImage : Conversation()

}

/**
 * Generate a signature of an image url to have a unique
 * entry in cache for each usage of image url in the app.
 *
 * It should be used when an image url is used in multiple
 * location, with different width / height.
 */
fun ImageSignatureKeys.generateSignature(uri: Uri?) =
    "[${javaClass.canonicalName}]${uri ?: "BLANK"}"