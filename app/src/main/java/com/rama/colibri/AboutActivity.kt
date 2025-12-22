package com.rama.colibri

import android.app.Activity
import android.os.Bundle
import android.view.View

class AboutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        setContentView(R.layout.about_view)

        val root = findViewById<View>(android.R.id.content)
        root.setOnApplyWindowInsetsListener { v, insets ->
            val topInset = insets.systemWindowInsetTop
            v.setPadding(v.paddingLeft, topInset, v.paddingRight, v.paddingBottom)
            insets
        }
    }
}