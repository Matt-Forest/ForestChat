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
package com.forest.forestchat.ui.common.circleProfile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.forest.forestchat.R
import com.forest.forestchat.extensions.asColor
import com.forest.forestchat.extensions.dp
import com.forest.forestchat.extensions.sp

class CircleProfileView : View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val textPaintValue = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = ResourcesCompat.getFont(context, R.font.mulish_regular)
        textSize = 16.sp.toFloat()
        letterSpacing = 0.15f
        color = R.color.white.asColor(context)
    }

    init {
        minimumHeight = 24.dp
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val center = height / 2
        fillPaint.color = R.color.primary.asColor(context)

        canvas.drawCircle(center.toFloat(), center.toFloat(), center.toFloat(), fillPaint)

        getBitmapFromVectorDrawable(R.drawable.ic_profile)?.let { image ->
            val x = width / 2 - image.width / 2
            val y = height / 2 - image.height / 2
            canvas.drawBitmap(image, x.toFloat(), y.toFloat(), fillPaint)
        }
    }

    private fun getBitmapFromVectorDrawable(@DrawableRes drawableId: Int): Bitmap? =
        ContextCompat.getDrawable(context, drawableId)?.let { drawable ->
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val drawableSize = 16.dp

            val canvas = Canvas(bitmap)
            val margin = (width - drawableSize) / 2
            drawable.setBounds(margin, margin, width - margin, height - margin)
            drawable.draw(canvas)
            return bitmap
        }

}