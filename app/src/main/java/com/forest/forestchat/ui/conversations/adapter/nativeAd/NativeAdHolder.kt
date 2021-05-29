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
package com.forest.forestchat.ui.conversations.adapter.nativeAd

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderNativeAdBinding
import com.forest.forestchat.extensions.asString
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.common.avatar.AvatarType
import com.forest.forestchat.ui.common.avatar.GroupAvatarView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAdView

class NativeAdHolder(
    parent: ViewGroup
) : BaseHolder<NativeAdItem>(parent, R.layout.holder_native_ad) {

    private val binding = HolderNativeAdBinding.bind(itemView)

    override fun bind(item: NativeAdItem) {
        val adLoader = AdLoader.Builder(context, R.string.ads_native_id.asString(context))
            .forNativeAd { nativeAd ->
                val adView = LayoutInflater.from(context)
                    .inflate(R.layout.view_native_ad, null) as NativeAdView

                val title = adView.findViewById<TextView>(R.id.title)
                val snippet = adView.findViewById<TextView>(R.id.snippet)
                val image = adView.findViewById<ImageView>(R.id.image)
                val icon = adView.findViewById<GroupAvatarView>(R.id.icon)

                title.text = nativeAd.headline
                snippet.text = nativeAd.body
                nativeAd.icon?.uri?.let { uri ->
                    icon.updateAvatars(AvatarType.Single.Image(uri.toString()))
                }
                when (val uri = nativeAd.icon?.uri) {
                    null -> icon.updateAvatars(AvatarType.Single.Ads)
                    else -> icon.updateAvatars(AvatarType.Single.Image(uri.toString()))
                }
                image.setImageDrawable(nativeAd.images[0].drawable)

                adView.setNativeAd(nativeAd)
                binding.container.removeAllViews()
                binding.container.addView(adView)
            }
            .withAdListener(object : AdListener() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    Toast.makeText(context, "test", Toast.LENGTH_SHORT).show()
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

}