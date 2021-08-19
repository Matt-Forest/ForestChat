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
package com.forest.forestchat.manager

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.transform.CircleCropTransformation
import com.forest.forestchat.R
import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.domain.useCases.CountUnreadUseCase
import com.forest.forestchat.domain.useCases.GetPinnedShortCutConversationUseCase
import com.forest.forestchat.ui.NavigationActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import me.leolin.shortcutbadger.ShortcutBadger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForestChatShortCutManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val countUnreadUseCase: CountUnreadUseCase,
    private val getPinnedShortCutConversationUseCase: GetPinnedShortCutConversationUseCase
) {

    suspend fun updateBadge() {
        val count = countUnreadUseCase()
        ShortcutBadger.applyCount(context, count)
    }

    suspend fun updateShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager =
                context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
            if (shortcutManager.isRateLimitingActive) return

            shortcutManager.dynamicShortcuts = getPinnedShortCutConversationUseCase()
                ?.take(shortcutManager.maxShortcutCountPerActivity - shortcutManager.manifestShortcuts.size)
                ?.map { conversation ->
                    createShortcutForConversation(
                        conversation,
                        shortcutManager
                    )
                } ?: listOf()
        }
    }

    @TargetApi(25)
    private suspend fun createShortcutForConversation(
        conversation: Conversation,
        shortcutManager: ShortcutManager
    ): ShortcutInfo {
        val icon = when (conversation.recipients.size) {
            1 -> {
                val address = conversation.recipients.first().address
                val bitmap = buildIconBitmap("tel:$address", shortcutManager)

                when (bitmap == null) {
                    true ->Icon.createWithResource(context, R.mipmap.shortcut_person)
                    false -> Icon.createWithBitmap(bitmap)
                }
            }
            else -> Icon.createWithResource(context, R.mipmap.shortcut_group)
        }

        val intent = Intent(context, NavigationActivity::class.java)
            .setAction(Intent.ACTION_VIEW)
            .putExtra(NavigationActivity.ThreadId, conversation.id)

        return ShortcutInfo.Builder(context, "${conversation.id}")
            .setShortLabel(conversation.getTitle())
            .setLongLabel(conversation.getTitle())
            .setIcon(icon)
            .setIntent(intent)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private suspend fun buildIconBitmap(uri: String?, shortcutManager: ShortcutManager): Bitmap? {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(uri)
            .size(shortcutManager.iconMaxWidth, shortcutManager.iconMaxHeight)
            .transformations(CircleCropTransformation())
            .allowHardware(false) // Disable hardware bitmaps.
            .build()

        return when (val res = loader.execute(request)) {
            is SuccessResult -> (res.drawable as BitmapDrawable).bitmap
            is ErrorResult -> null
        }
    }

}