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
package com.forest.forestchat.ui.common.avatar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.forest.forestchat.databinding.ViewGroupAvatarBinding

class GroupAvatarView : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    private val binding: ViewGroupAvatarBinding

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = ViewGroupAvatarBinding.inflate(layoutInflater, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {
            updateAvatars(AvatarType.Single.Profile)
        }
    }

    fun updateAvatars(type: AvatarType) {
        with(binding) {
            avatarForegroundFrame.updateLayoutParams<LayoutParams> {
                matchConstraintPercentWidth = if (type is AvatarType.Group) 0.75f else 1.0f
            }
            avatarBackground.isVisible = type is AvatarType.Group

            when (type) {
                is AvatarType.Single -> avatarForeground.setAvatar(type)
                is AvatarType.Group -> {
                    avatarForeground.setAvatar(type.foreground)
                    avatarBackground.setAvatar(type.background)
                }
            }
        }
    }

}