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
import android.os.Build
import android.widget.ImageView
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import coil.size.Precision
import coil.size.Scale
import com.forest.forestchat.R

fun ImageView.loadUri(uri: Uri?, imageSignatureKeys: ImageSignatureKeys?) {
    load(uri) {
        imageSignatureKeys?.generateSignature(uri)?.let { key ->
            memoryCacheKey(key)
        }

        placeholder(R.drawable.ic_image)
        error(R.drawable.ic_image)
        fallback(R.drawable.ic_image)
    }
}

fun ImageView.loadUriGif(uri: Uri?, size: Int, imageSignatureKeys: ImageSignatureKeys?) {
    val imageLoader = ImageLoader.Builder(context)
        .componentRegistry {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder(context))
            } else {
                add(GifDecoder())
            }
        }
        .build()
    load(uri, imageLoader) {
        size(size)

        imageSignatureKeys?.generateSignature(uri)?.let { key ->
            memoryCacheKey(key)
        }

        placeholder(R.drawable.ic_image)
        error(R.drawable.ic_image)
        fallback(R.drawable.ic_image)
    }
}