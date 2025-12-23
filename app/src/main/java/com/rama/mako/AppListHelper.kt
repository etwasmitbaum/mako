package com.rama.mako

import android.content.Context
import android.content.Intent
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast

class AppListHelper(private val context: Context, private val listView: ListView) {

    fun setup() {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = pm.queryIntentActivities(intent, 0)
            .sortedBy { it.loadLabel(pm).toString().lowercase() }

        val labels = apps.map { it.loadLabel(pm).toString() }
        listView.adapter = ArrayAdapter(context, R.layout.app_list_item, R.id.text1, labels)

        listView.setOnItemClickListener { _, _, position, _ ->
            if (position >= apps.size) return@setOnItemClickListener
            val app = apps[position]
            val launchIntent = Intent().apply {
                setClassName(app.activityInfo.packageName, app.activityInfo.name)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(launchIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "App not found or uninstalled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
