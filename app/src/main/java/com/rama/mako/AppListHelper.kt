package com.rama.mako

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class AppListHelper(
    private val context: Context,
    private val listView: ListView
) {

    private val prefs =
        context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    // Package name of the row currently showing actions
    private var openActionsFor: String? = null

    fun setup() {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = pm.queryIntentActivities(intent, 0).toMutableList()

        fun sortApps() {
            apps.sortWith(
                compareByDescending<ResolveInfo> {
                    prefs.getBoolean(it.activityInfo.packageName, false)
                }.thenBy {
                    it.loadLabel(pm).toString().lowercase()
                }
            )
        }

        sortApps()

        val adapter = object : ArrayAdapter<ResolveInfo>(
            context,
            R.layout.app_list_item,
            R.id.open_app_button,
            apps
        ) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getView(position, convertView, parent)
                val app = getItem(position) ?: return view

                val pkg = app.activityInfo.packageName

                val label = view.findViewById<TextView>(R.id.open_app_button)
                val favButton = view.findViewById<View>(R.id.favorite_button)
                val favIcon = view.findViewById<ImageView>(R.id.favorite_icon)
                val closeButton = view.findViewById<View>(R.id.close_button)
                val actions = view.findViewById<View>(R.id.actions_container)

                label.text = app.loadLabel(pm)

                // Long click
                view.setOnClickListener {
                    // If actions are open → close them
                    if (openActionsFor == pkg) {
                        openActionsFor = null
                        notifyDataSetChanged()
                        return@setOnClickListener
                    }

                    val launchIntent = Intent().apply {
                        setClassName(pkg, app.activityInfo.name)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    try {
                        context.startActivity(launchIntent)
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "App not found or uninstalled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                // Restore favorite state
                favIcon.isSelected = prefs.getBoolean(pkg, false)

                // Show/hide actions
                actions.visibility =
                    if (openActionsFor == pkg) View.VISIBLE else View.GONE

                // Long press → show actions
                view.setOnLongClickListener {
                    openActionsFor = pkg
                    notifyDataSetChanged()
                    true
                }

                // Favorite toggle
                favButton.setOnClickListener {
                    val newState = !favIcon.isSelected
                    favIcon.isSelected = newState

                    prefs.edit()
                        .putBoolean(pkg, newState)
                        .apply()

                    sortApps()
                    notifyDataSetChanged()
                }

                // Close actions
                closeButton.setOnClickListener {
                    openActionsFor = null
                    notifyDataSetChanged()
                }

                return view
            }
        }

        listView.adapter = adapter

//        listView.setOnItemClickListener { _, _, position, _ ->
//            val app = apps.getOrNull(position) ?: return@setOnItemClickListener
//            val pkg = app.activityInfo.packageName
//
//            // If actions are open, close them instead of launching
//            if (openActionsFor == pkg) {
//                openActionsFor = null
//                adapter.notifyDataSetChanged()
//                return@setOnItemClickListener
//            }
//
//            val launchIntent = Intent().apply {
//                setClassName(pkg, app.activityInfo.name)
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
//
//            try {
//                context.startActivity(launchIntent)
//            } catch (e: Exception) {
//                Toast.makeText(
//                    context,
//                    "App not found or uninstalled",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
    }
}
