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
package com.forest.forestchat.app

import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.forest.forestchat.ui.common.coil.ByteArrayFetcher
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ForestChatApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.getDefaultNightMode())
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .componentRegistry {
            add(ByteArrayFetcher())
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder(this@ForestChatApp))
            } else {
                add(GifDecoder())
            }
        }
        .build()

}