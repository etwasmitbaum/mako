package com.rama.mako

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var timeText: TextView
    private lateinit var dateText: TextView
    private lateinit var batteryText: TextView
    private lateinit var listView: ListView

    private lateinit var clockManager: ClockManager
    private lateinit var batteryHelper: BatteryManagerHelper

    private val settingsPrefs by lazy {
        getSharedPreferences("settings", MODE_PRIVATE)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allow layout behind system bars
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        setContentView(R.layout.view_home)

        // Root safe-area padding
        val root = findViewById<View>(R.id.root)
        root.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(
                insets.systemWindowInsetLeft + dp(32),
                insets.systemWindowInsetTop + dp(32),
                insets.systemWindowInsetRight + dp(32),
                insets.systemWindowInsetBottom + dp(32)
            )
            insets
        }

        // Views
        timeText = findViewById(R.id.time)
        dateText = findViewById(R.id.date)
        batteryText = findViewById(R.id.battery)
        listView = findViewById(R.id.appList)

        // Clock
        clockManager = ClockManager(
            timeTextView = timeText,
            dateTextView = dateText,
            prefs = prefs
        )
        clockManager.start()

        // Battery
        batteryHelper = BatteryManagerHelper(this) { status ->
            batteryText.text = status
        }
        batteryHelper.register()

        // App list
        AppListHelper(this, listView).setup()

        // Clock click → open system clock
        timeText.setOnClickListener { openSystemClock() }

        // Settings
        findViewById<View>(R.id.settings_button).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        syncSettings()
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryHelper.unregister()
    }

    // -------------------------
    // Helpers
    // -------------------------

    private fun syncSettings() {
        val showClock = settingsPrefs.getBoolean("show_clock", true)
        val showDate = settingsPrefs.getBoolean("show_date", true)
        val showBattery = settingsPrefs.getBoolean("show_battery", true)

        timeText.visibility = if (showClock) View.VISIBLE else View.GONE
        findViewById<View>(R.id.date_row).visibility = if (showDate) View.VISIBLE else View.GONE
        findViewById<View>(R.id.battery_row).visibility =
            if (showBattery) View.VISIBLE else View.GONE
    }


    private fun openSystemClock() {
        val pm = packageManager

        val intents = listOf(
            Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_CLOCK"),
            Intent("android.intent.action.SHOW_ALARMS"),
            Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_ALARM")
        )

        for (intent in intents) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (intent.resolveActivity(pm) != null) {
                startActivity(intent)
                return
            }
        }

        Toast.makeText(this, "No clock app found", Toast.LENGTH_SHORT).show()
    }
}
