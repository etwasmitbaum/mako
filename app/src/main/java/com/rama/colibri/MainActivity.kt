package com.rama.colibri

import android.app.Activity
import android.content.*
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {

    private lateinit var timeText: TextView
    private lateinit var dateText: TextView
    private lateinit var listView: ListView

    private val handler = Handler(Looper.getMainLooper())
    private var batteryPercent: String? = null

    /* ---------- Lifecycle ---------- */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen (API 26–29 safe)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        setContentView(R.layout.activity_main)

        timeText = findViewById(R.id.time)
        dateText = findViewById(R.id.date)
        listView = findViewById(R.id.appList)

        setupAppList()
        startClock()
        registerBatteryReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        handler.removeCallbacksAndMessages(null)
    }

    /* ---------- Time & Date ---------- */

    private fun startClock() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val runnable = object : Runnable {
            override fun run() {
                val now = Date()
                timeText.text = timeFormat.format(now)
                updateDateLine(dateFormat.format(now))
                handler.postDelayed(this, 1000)
            }
        }

        handler.post(runnable)
    }

    private fun updateDateLine(date: String) {
        dateText.text = buildStatusLine(date, batteryPercent)
    }

    private fun buildStatusLine(date: String?, battery: String?): String {
        return listOfNotNull(date, battery).joinToString("  |  ")
    }

    /* ---------- Battery ---------- */

    private fun registerBatteryReceiver() {
        registerReceiver(
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return

            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            if (level >= 0 && scale > 0) {
                batteryPercent = "${(level * 100 / scale.toFloat()).toInt()}%"
                updateDateLine(
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(Date())
                )
            }
        }
    }

    /* ---------- App list ---------- */

    private fun setupAppList() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = pm.queryIntentActivities(intent, 0)
            .sortedBy { it.loadLabel(pm).toString().lowercase() }

        val labels = apps.map { it.loadLabel(pm).toString() }

        val footer = layoutInflater.inflate(
            R.layout.list_footer_about,
            listView,
            false
        )

        listView.addFooterView(footer)

        listView.adapter = ArrayAdapter(
            this,
            R.layout.app_list_item,
            R.id.text1,
            labels
        )

        listView.setOnItemClickListener { _, _, position, _ ->
            if (position >= apps.size) return@setOnItemClickListener

            val app = apps[position]
            val launchIntent = Intent().apply {
                setClassName(
                    app.activityInfo.packageName,
                    app.activityInfo.name
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(launchIntent)
        }

        footer.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }
}
