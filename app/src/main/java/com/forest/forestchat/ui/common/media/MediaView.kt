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
package com.forest.forestchat.ui.common.media

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.forest.forestchat.R
import com.forest.forestchat.extensions.asColor
import com.forest.forestchat.extensions.dp

class MediaView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AppCompatImageView(context, attrs) {

    enum class RoundedStyle(
        val topLeft: Boolean,
        val topRight: Boolean,
        val bottomRight: Boolean,
        val bottomLeft: Boolean
    ) {
        Alone(true, true, true, true),
        Middle(false, false, false, false),
        Left(true, false, false, true),
        Right(false, true, true, false),
        TopLeft(true, false, false, false),
        TopRight(false, true, false, false),
        BottomRight(false, false, true, false),
        BottomLeft(false, false, false, true)
    }

    private var style = RoundedStyle.Alone
    private var isVideo = false
    private val path = Path()
    private val radius = 10.dp.toFloat()
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = R.color.primary.asColor(context)
    }

    init {
        scaleType = ScaleType.CENTER_CROP
        adjustViewBounds = true
    }

    fun setStyle(style: RoundedStyle, isVideo: Boolean) {
        this.isVideo = isVideo
        this.style = style
        setPath()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setPath()
    }

    override fun onDraw(canvas: Canvas) {
        if (!path.isEmpty) {
            canvas.clipPath(path)
        }

        super.onDraw(canvas)

        if (isVideo) {
            ContextCompat.getDrawable(context, R.drawable.ic_play)?.let { drawable ->
                getBitmapFromVectorDrawable(drawable)?.let { image ->
                    canvas.drawBitmap(image, 0F, 0F, fillPaint)
                }
            }
        }
    }

    fun setSize() {
        val params = layoutParams
        when (style) {
            RoundedStyle.Alone -> {
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            RoundedStyle.Left,
            RoundedStyle.Right -> {
                params.width = 144.dp
                params.height = 144.dp
            }
            else -> {
                params.width = 94.dp
                params.height = 94.dp
            }
        }
        layoutParams = params
    }

    private fun setPath() {
        path.rewind()

        val width = width.toFloat()
        val height = height.toFloat()

        val cornerRect = RectF().apply { set(-radius, -radius, radius, radius) }

        if (style.topLeft) {
            cornerRect.offsetTo(0f, 0f)
            path.arcTo(cornerRect, 180f, 90f)
        }

        if (style.topRight) {
            cornerRect.offsetTo(width - radius * 2, 0f)
            path.arcTo(cornerRect, 270f, 90f)
        }

        if (style.bottomRight) {
            cornerRect.offsetTo(width - radius * 2, height - radius * 2)
            path.arcTo(cornerRect, 0f, 90f)
        }

        if (style.bottomLeft) {
            cornerRect.offsetTo(0f, height - radius * 2)
            path.arcTo(cornerRect, 90f, 90f)
        }

        path.close()
    }

    private fun getBitmapFromVectorDrawable(drawable: Drawable): Bitmap? {
        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }

}