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
package com.forest.forestchat.ui.common

import android.content.Context
import android.os.Build
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.ProgressBar
import com.forest.forestchat.R
import com.forest.forestchat.extensions.asDrawable

class HorizontalProgressBar : ProgressBar {

    companion object {
        private const val STEP = 20
        private const val START = 0
        private const val MAX_BEFORE_PAUSE = 60
        private const val MAX = 100
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs,
        android.R.style.Widget_ProgressBar_Large
    )

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    private val nextLoadingStepRunnable = Runnable { showNextLoadingStep() }
    private val hideViewRunnable = Runnable { hide() }

    init {
        hide()
        isIndeterminate = false
        progressDrawable = R.drawable.progress_horizontal.asDrawable(context)
        max = MAX
        progress = START
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (progress in START + 1 until MAX) {
            show()
            showNextLoadingStep()
        }
    }

    private fun hide() {
        alpha = 0f
    }

    private fun show() {
        alpha = 1f
    }

    fun startLoading() {
        if (progress !in START + 1 until MAX) {
            removeAllCallbacks()

            progress = 0
            show()
            showNextLoadingStep()
        }
    }

    fun stopLoading() {
        removeAllCallbacks()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setProgress(MAX, true)
        } else {
            progress = MAX
        }

        // 80 is the progress bar animation duration, cf ProgressBar.PROGRESS_ANIM_DURATION which
        // is a private const
        postDelayed(hideViewRunnable, 300)
    }

    private fun removeAllCallbacks() {
        // remove all callbacks not reached
        removeCallbacks(nextLoadingStepRunnable)
        removeCallbacks(hideViewRunnable)
    }

    private fun showNextLoadingStep() {
        val current = progress
        if (current < MAX_BEFORE_PAUSE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setProgress((current + STEP).coerceAtMost(MAX_BEFORE_PAUSE), true)
            } else {
                progress = MAX
            }
            postDelayed(nextLoadingStepRunnable, 500)
        }
    }

}