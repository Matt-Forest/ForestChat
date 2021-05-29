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
package com.forest.forestchat.ui.conversations.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.forest.forestchat.databinding.DialogConversationOptionsBinding
import com.forest.forestchat.extensions.visibleIf
import com.forest.forestchat.ui.base.dialog.BaseBottomDialog
import com.google.android.material.bottomsheet.BottomSheetDialog

class ConversationOptionsDialog(
    context: Context,
    private val optionSelected: (ConversationOptionType) -> Unit,
    private val showAddToContacts: Boolean,
    private val showPin: Boolean,
    private val showPinnedOff: Boolean,
    private val showMarkAsRead: Boolean
) : BaseBottomDialog(context, true) {

    private val binding: DialogConversationOptionsBinding by lazy {
        val layoutInflater = LayoutInflater.from(context)
        DialogConversationOptionsBinding.inflate(layoutInflater)
    }

    override fun getView(): View = binding.root

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        with(binding) {
            addContact.visibleIf { showAddToContacts }
            pin.visibleIf { showPin }
            pinnedOff.visibleIf { showPinnedOff }
            markAsRead.visibleIf { showMarkAsRead }

            addContact.setOnClickListener { select(dialog, ConversationOptionType.AddToContacts) }
            pin.setOnClickListener { select(dialog, ConversationOptionType.Pin) }
            pinnedOff.setOnClickListener { select(dialog, ConversationOptionType.PinnedOff) }
            markAsRead.setOnClickListener { select(dialog, ConversationOptionType.MarkAsRead) }
            archive.setOnClickListener { select(dialog, ConversationOptionType.Archive) }
            block.setOnClickListener { select(dialog, ConversationOptionType.Block) }
            remove.setOnClickListener { select(dialog, ConversationOptionType.Remove) }
        }
    }

    private fun select(dialog: BottomSheetDialog, type: ConversationOptionType) {
        dialog.dismiss()
        optionSelected(type)
    }

}