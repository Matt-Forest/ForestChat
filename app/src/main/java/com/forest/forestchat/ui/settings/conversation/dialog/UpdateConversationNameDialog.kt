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
package com.forest.forestchat.ui.settings.conversation.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.forest.forestchat.databinding.DialogUpdateConversationNameBinding
import com.forest.forestchat.ui.base.dialog.BaseAlertDialog

class UpdateConversationNameDialog(
    context: Context,
    private val name: String,
    private val onConfirm: ((String) -> Unit)
) : BaseAlertDialog(context) {

    private val binding: DialogUpdateConversationNameBinding by lazy {
        val layoutInflater = LayoutInflater.from(context)
        DialogUpdateConversationNameBinding.inflate(layoutInflater)
    }

    override fun getView(): View = binding.root

    override fun onDialogCreated(dialog: AlertDialog) {
        with(binding) {
            updateTitle.setText(name)
            confirm.setOnClickListener {
                dialog.dismiss()
                onConfirm.invoke(updateTitle.text.toString())
            }
            refuse.setOnClickListener {
                dialog.dismiss()
            }
        }
    }

}