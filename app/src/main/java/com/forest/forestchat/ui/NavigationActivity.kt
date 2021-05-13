package com.forest.forestchat.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import com.forest.forestchat.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemUI()

        window.statusBarColor = when(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            true -> Color.parseColor("#00000000")
            false -> Color.parseColor("#66000000")
        }

        val contentViewAction = { setContentView(R.layout.activity_navigation) }

        val hasSavedInstanceState = savedInstanceState != null
        if(hasSavedInstanceState) {
            // To properly restore instance state, we need to directly
            // set content view to properly restore instance state.
            contentViewAction()
        }
    }

    private fun hideSystemUI() {
        with(window) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(false)
                insetsController?.let {
                    it.hide(WindowInsets.Type.statusBars())
                    it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            }
        }
    }

}