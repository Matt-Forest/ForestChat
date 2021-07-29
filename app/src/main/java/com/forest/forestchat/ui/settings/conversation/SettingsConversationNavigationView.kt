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
package com.forest.forestchat.ui.settings.conversation

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TableRow
import androidx.navigation.findNavController
import coil.size.PixelSize
import com.forest.forestchat.R
import com.forest.forestchat.databinding.NavigationSettingsConversationBinding
import com.forest.forestchat.extensions.*
import com.forest.forestchat.ui.common.dialog.ConversationDeleteDialog
import com.forest.forestchat.ui.common.mappers.buildAvatar
import com.forest.forestchat.ui.common.media.Media
import com.forest.forestchat.ui.common.media.MediaView
import com.forest.forestchat.ui.gallery.GalleryInput
import com.forest.forestchat.ui.recipients.models.RecipientsInput
import com.forest.forestchat.ui.settings.conversation.dialog.UpdateConversationNameDialog
import com.forest.forestchat.ui.settings.conversation.models.SettingsConversationData
import com.forest.forestchat.ui.settings.conversation.models.SettingsConversationEvent

class SettingsConversationNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding: NavigationSettingsConversationBinding

    lateinit var onMediaSelected: (Long) -> Unit
    lateinit var onProfileSelected: () -> Unit
    lateinit var onProfileLongClick: () -> Unit
    lateinit var onInformationAction: () -> Unit
    lateinit var onTitleChange: () -> Unit
    lateinit var onTitleUpdated: (String) -> Unit
    lateinit var onNotifications: () -> Unit
    lateinit var onArchive: () -> Unit
    lateinit var onBlock: () -> Unit
    lateinit var onRemove: () -> Unit

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationSettingsConversationBinding.inflate(layoutInflater, this)

        orientation = VERTICAL

        with(binding) {
            back.setOnClickListener { findNavController().popBackStack() }

            information.setOnClickListener { onProfileSelected() }
            information.setOnLongClickListener {
                onProfileLongClick()
                true
            }
            action.setOnClickListener { onInformationAction() }
            groupName.setOnClickListener { onTitleChange() }
            notifications.setOnClickListener { onNotifications() }
            archive.setOnClickListener { onArchive() }
            block.setOnClickListener { onBlock() }
            remove.setOnClickListener {
                ConversationDeleteDialog(context) {
                    onRemove()
                }
            }
        }
    }

    fun onEvent(event: SettingsConversationEvent) {
        when (event) {
            is SettingsConversationEvent.ShowGallery -> {
                val input = GalleryInput(event.medias, event.mediaSelected)
                findNavController().navigate(
                    SettingsConversationFragmentDirections.goToGallery(input)
                )
            }
            is SettingsConversationEvent.ShowTitleUpdate -> {
                UpdateConversationNameDialog(context, event.title) { newName ->
                    onTitleUpdated(newName)
                }.create().show()
            }
            is SettingsConversationEvent.GoToNotification -> {
                // TODO update notification screen
            }
            is SettingsConversationEvent.GoToGroupMembers -> {
                val input = RecipientsInput(event.recipients)
                findNavController().navigate(
                    SettingsConversationFragmentDirections.goToRecipients(
                        input
                    )
                )
            }
            else -> null
        }
    }

    fun updateData(data: SettingsConversationData) {
        with(binding) {
            when (data) {
                is SettingsConversationData.Single -> {
                    name.text = data.name
                    describe.text = data.recipients[0].getNumberPhone()
                    avatars.updateAvatars(buildAvatar(data.recipients))
                    action.visibleIf { data.showAddContact }
                    action.setImageDrawable(R.drawable.ic_add_profile.asDrawable(context))
                    groupName.gone()
                }
                is SettingsConversationData.Group -> {
                    name.text = data.name
                    describe.text = R.string.conversation_settings_members_count.format(
                        context,
                        data.recipients.size
                    )
                    avatars.updateAvatars(buildAvatar(data.recipients))
                    action.visible()
                    action.setImageDrawable(R.drawable.ic_chevron_right.asDrawable(context))
                    groupName.visible()
                }
            }
        }
    }

    fun updateMedias(medias: List<Media>) {
        binding.medias.removeAllViews()
        val displayMetrics = Resources.getSystem().displayMetrics
        val screenWidth = displayMetrics.widthPixels.toFloat()

        val mediasView = medias.map { media -> buildMediaView(media) }

        mediasView.chunked(3).forEach { mediasRow ->
            val rowView = TableRow(context)
            val lp = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            rowView.layoutParams = lp
            mediasRow.forEach { mediaView ->
                rowView.addView(mediaView)
                mediaView.setSize(screenWidth, mediasRow.size)
            }
            rowView.setPadding(0, 1.dp, 0, 1.dp)
            binding.medias.addView(rowView)
        }
    }

    private fun buildMediaView(media: Media): MediaView = MediaView(context).apply {
        when (media.isGif) {
            true -> loadUri(
                uri = media.uri,
                size = PixelSize(180.dp, 180.dp),
                imageSignatureKeys = ImageSignatureKeys.Conversation.Message
            )
            false -> loadUri(
                uri = media.uri,
                imageSignatureKeys = ImageSignatureKeys.Conversation.Message
            )
        }
        setStyle(MediaView.RoundedStyle.Middle, media.isVideo)

        setOnClickListener { onMediaSelected(media.mediaId) }
    }

    fun updateArchive(isArchive: Boolean) {
        binding.archive.text = when (isArchive) {
            true -> R.string.conversation_settings_unarchived
            false -> R.string.conversation_settings_archived
        }.asString(context)
    }

    fun updateBlock(isBlock: Boolean) {
        with(binding) {
            block.text = when (isBlock) {
                true -> R.string.conversation_settings_unblock
                false -> R.string.conversation_settings_block
            }.asString(context)

            archive.isEnabled = !isBlock
            notifications.isEnabled = !isBlock
        }
    }

}