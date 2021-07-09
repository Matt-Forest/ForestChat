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
package com.forest.forestchat.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.forest.forestchat.domain.useCases.MarkAsSeenUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MarkSeenReceiver : BroadcastReceiver() {

    companion object {
        const val ThreadId = "threadId"
    }

    @Inject
    lateinit var markAsSeenUseCase: MarkAsSeenUseCase

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.getLongExtra(MarkReadReceiver.ThreadId, 0)?.let { threadId ->
            CoroutineScope(Dispatchers.IO).launch {
                markAsSeenUseCase(threadId)
            }
        }
    }

}