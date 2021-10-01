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
package com.forest.forestchat.ui.common

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import com.forest.forestchat.utils.tryOrNull
import com.google.android.mms.ContentType

class ForestEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AppCompatEditText(context, attrs) {

    private var listener: Listener? = null

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    override fun onCreateInputConnection(editorInfo: EditorInfo): InputConnection {
        EditorInfoCompat.setContentMimeTypes(
            editorInfo, arrayOf(
                ContentType.IMAGE_JPEG,
                ContentType.IMAGE_JPG,
                ContentType.IMAGE_PNG,
                ContentType.IMAGE_GIF
            )
        )

        val callback = InputConnectionCompat
            .OnCommitContentListener { inputContentInfo, flags, _ ->
                val grantReadPermission =
                    flags and InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION != 0

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && grantReadPermission) {
                    return@OnCommitContentListener tryOrNull {
                        inputContentInfo.requestPermission()
                        listener?.onInputContentSelected(inputContentInfo)
                        true
                    } ?: false
                }

                true
            }

        return InputConnectionCompat.createWrapper(
            InputConnectionWrapper(super.onCreateInputConnection(editorInfo), true),
            editorInfo,
            callback
        )
    }

    interface Listener {
        fun onInputContentSelected(inputContentInfo: InputContentInfoCompat)
    }

}