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
package com.forest.forestchat.ui.conversation.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.forest.forestchat.databinding.DialogMessageOptionsBinding
import com.forest.forestchat.extensions.visibleIf
import com.forest.forestchat.ui.base.dialog.BaseBottomDialog
import com.google.android.material.bottomsheet.BottomSheetDialog

class MessageOptionsDialog(
    context: Context,
    private val canCopy: Boolean,
    private val optionSelected: (MessageOptionType) -> Unit
) : BaseBottomDialog(context, true) {

    private val binding: DialogMessageOptionsBinding by lazy {
        val layoutInflater = LayoutInflater.from(context)
        DialogMessageOptionsBinding.inflate(layoutInflater)
    }

    override fun getView(): View = binding.root

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        with(binding) {
            copy.visibleIf { canCopy }

            copy.setOnClickListener { select(dialog, MessageOptionType.Copy) }
            details.setOnClickListener { select(dialog, MessageOptionType.Details) }
            remove.setOnClickListener { select(dialog, MessageOptionType.Remove) }
        }
    }

    private fun select(dialog: BottomSheetDialog, type: MessageOptionType) {
        dialog.dismiss()
        optionSelected(type)
    }

}