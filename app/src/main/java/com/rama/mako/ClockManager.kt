package com.rama.mako

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class ClockManager(
    private val timeTextView: TextView,
    private val dateTextView: TextView
) {
    private val handler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    private val runnable = object : Runnable {
        override fun run() {
            val now = Date()
            timeTextView.text = timeFormat.format(now)
            dateTextView.text = dateFormat.format(now)
            handler.postDelayed(this, 1000)
        }
    }

    fun start() = handler.post(runnable)
    fun stop() = handler.removeCallbacks(runnable)
}

