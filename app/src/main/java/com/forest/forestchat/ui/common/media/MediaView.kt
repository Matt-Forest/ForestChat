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
import androidx.core.view.setPadding
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
    private val radiusLarge = 10.dp.toFloat()
    private val radiusSmall = 2.dp.toFloat()
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = R.color.primary.asColor(context)
    }

    init {
        scaleType = ScaleType.CENTER_CROP
        setPadding(1.dp, 0, 1.dp, 0)
    }

    fun setStyle(style: RoundedStyle, isVideo: Boolean) {
        this.isVideo = isVideo
        this.style = style
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

    fun setSize(tableWidth: Float, nbItemByRow: Int) {
        val params = layoutParams
        when (style) {
            RoundedStyle.Alone -> {
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            RoundedStyle.Left,
            RoundedStyle.Right -> {
                val size = (tableWidth / nbItemByRow).toInt()
                params.width = size
                params.height = size
            }
            else -> {
                val size = (tableWidth / 3).toInt()
                params.width = size
                params.height = size
            }
        }
        layoutParams = params
    }

    private fun setPath() {
        path.rewind()

        val width = width.toFloat()
        val height = height.toFloat()

        val cornerRectSmall =
            RectF().apply { set(-radiusSmall, -radiusSmall, radiusSmall, radiusSmall) }
        val cornerRectLarge =
            RectF().apply { set(-radiusLarge, -radiusLarge, radiusLarge, radiusLarge) }

        if (style.topLeft) {
            cornerRectLarge.offsetTo(0f, 0f)
            path.arcTo(cornerRectLarge, 180f, 90f)
        } else {
            cornerRectSmall.offsetTo(0f, 0f)
            path.arcTo(cornerRectSmall, 180f, 90f)
        }

        if (style.topRight) {
            cornerRectLarge.offsetTo(width - radiusLarge * 2, 0f)
            path.arcTo(cornerRectLarge, 270f, 90f)
        } else {
            cornerRectSmall.offsetTo(width - radiusSmall * 2, 0f)
            path.arcTo(cornerRectSmall, 270f, 90f)
        }

        if (style.bottomRight) {
            cornerRectLarge.offsetTo(width - radiusLarge * 2, height - radiusLarge * 2)
            path.arcTo(cornerRectLarge, 0f, 90f)
        } else {
            cornerRectSmall.offsetTo(width - radiusSmall * 2, height - radiusSmall * 2)
            path.arcTo(cornerRectSmall, 0f, 90f)
        }

        if (style.bottomLeft) {
            cornerRectLarge.offsetTo(0f, height - radiusLarge * 2)
            path.arcTo(cornerRectLarge, 90f, 90f)
        } else {
            cornerRectSmall.offsetTo(0f, height - radiusSmall * 2)
            path.arcTo(cornerRectSmall, 90f, 90f)
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