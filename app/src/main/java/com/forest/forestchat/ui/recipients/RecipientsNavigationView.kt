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
package com.forest.forestchat.ui.recipients

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.navigation.findNavController
import com.forest.forestchat.databinding.NavigationRecipientsBinding
import com.forest.forestchat.domain.models.Recipient
import com.forest.forestchat.ui.recipients.adapter.RecipientsAdapter

class RecipientsNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding: NavigationRecipientsBinding

    lateinit var onRecipientSelected: (Long) -> Unit

    private var recipientsAdapter = RecipientsAdapter { onRecipientSelected(it) }

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationRecipientsBinding.inflate(layoutInflater, this)

        orientation = VERTICAL

        binding.back.setOnClickListener { findNavController().popBackStack() }
        binding.recipientsRecycler.adapter = recipientsAdapter
    }

    fun setupRecipients(recipients: List<Recipient>) {
        recipientsAdapter.updateRecipients(recipients)
    }

}