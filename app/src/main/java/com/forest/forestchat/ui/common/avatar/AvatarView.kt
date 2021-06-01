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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.forest.forestchat.R
import com.forest.forestchat.extensions.asColor
import com.forest.forestchat.extensions.asFont
import com.forest.forestchat.extensions.sp


class AvatarView : View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = R.color.primary.asColor(context)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = R.font.mulish_regular.asFont(context)
        textSize = 16.sp.toFloat()
        letterSpacing = 0.0015f
        textAlign = Paint.Align.CENTER
        color = R.color.white.asColor(context)
    }

    private var center: Int = 0
    private var padding: Int = 0
    private var avatarType: AvatarType.Single = AvatarType.Single.Profile

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setAvatar(type: AvatarType.Single) {
        avatarType = type
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        padding = width / 5
        center = height / 2
        buildSingleAvatar(avatarType, canvas)
    }

    private fun buildSingleAvatar(type: AvatarType.Single, canvas: Canvas) {
        when (type) {
            AvatarType.Single.Profile -> {
                canvas.drawCircle(center.toFloat(), center.toFloat(), center.toFloat(), fillPaint)
                buildProfile(canvas)
            }
            AvatarType.Single.Ads -> {
                fillPaint.color = R.color.error.asColor(context)
                canvas.drawCircle(center.toFloat(), center.toFloat(), center.toFloat(), fillPaint)
                buildAds(canvas)
            }
            is AvatarType.Single.Letters -> {
                canvas.drawCircle(center.toFloat(), center.toFloat(), center.toFloat(), fillPaint)
                buildLetters(type.letters, canvas)
            }
            is AvatarType.Single.Image -> buildImage(type.uri, canvas)
        }
    }

    private fun buildProfile(canvas: Canvas) {
        ContextCompat.getDrawable(context, R.drawable.ic_profile)?.let { drawable ->
            getBitmapFromVectorDrawable(drawable, padding)?.let { image ->
                canvas.drawBitmap(image, 0F, 0F, fillPaint)
            }
        }
    }

    private fun buildLetters(letters: String, canvas: Canvas) {
        val xPos = center
        val yPos = (center - (textPaint.descent() + textPaint.ascent()) / 2).toInt()
        //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.

        canvas.drawText(letters, xPos.toFloat(), yPos.toFloat(), textPaint)
    }

    private fun buildImage(image: String, canvas: Canvas) {
        val imageLoader = context.imageLoader
        val request = ImageRequest.Builder(context)
            .data(image)
            .transformations(CircleCropTransformation())
            .target(
                onSuccess = { result ->
                    getBitmapFromVectorDrawable(result, 0)?.let { image ->
                        canvas.drawBitmap(image, 0F, 0F, fillPaint)
                        invalidate()
                    }
                },
                onError = {
                    buildProfile(canvas)
                })
            .build()
        imageLoader.enqueue(request)
    }

    private fun buildAds(canvas: Canvas) {
        ContextCompat.getDrawable(context, R.drawable.ic_ad)?.let { drawable ->
            getBitmapFromVectorDrawable(drawable, padding)?.let { image ->
                canvas.drawBitmap(image, 0F, 0F, fillPaint)
            }
        }
    }

    private fun getBitmapFromVectorDrawable(drawable: Drawable, padding: Int): Bitmap? {
        val bitmap = Bitmap.createBitmap(
            width - padding,
            height - padding,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.setBounds(padding, padding, width - padding, height - padding)
        drawable.draw(canvas)
        return bitmap
    }

}