package com.saggitt.omega.hidden

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R

class HiddenAppsSettingsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private val fullAppList = ArrayList<AppSelectionItem>()
    private val displayedList = ArrayList<AppSelectionItem>()
    private val selectedPackages = HashSet<String>()
    private lateinit var adapter: AppSelectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps_settings)

        selectedPackages.addAll(HiddenAppsManager.getHiddenPackages(this))

        findViewById<View>(R.id.btn_back).setOnClickListener {
            saveAndClose()
        }

        searchInput = findViewById(R.id.hidden_settings_search)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        recyclerView = findViewById(R.id.hidden_settings_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AppSelectionAdapter(displayedList, selectedPackages) { item, isChecked ->
            if (isChecked) {
                selectedPackages.add(item.packageName)
            } else {
                selectedPackages.remove(item.packageName)
            }
        }
        recyclerView.adapter = adapter

        loadInstalledApps()
    }

    override fun onBackPressed() {
        saveAndClose()
    }

    private fun saveAndClose() {
        HiddenAppsManager.saveHiddenPackages(this, selectedPackages)
        LauncherAppState.getInstance(this).model.forceReload()
        finish()
    }

    private fun filterApps(query: String) {
        displayedList.clear()
        if (query.isBlank()) {
            displayedList.addAll(fullAppList)
        } else {
            val q = query.trim().lowercase()
            for (item in fullAppList) {
                if (item.label.lowercase().contains(q) || item.packageName.lowercase().contains(q)) {
                    displayedList.add(item)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun loadInstalledApps() {
        fullAppList.clear()
        displayedList.clear()
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == packageName) continue

            val label = resolveInfo.loadLabel(pm).toString()
            val icon = resolveInfo.loadIcon(pm)
            fullAppList.add(AppSelectionItem(label, pkg, icon))
        }

        fullAppList.sortBy { it.label.lowercase() }
        filterApps(searchInput.text?.toString().orEmpty())
    }

    data class AppSelectionItem(
        val label: String,
        val packageName: String,
        val icon: Drawable
    )

    class AppSelectionAdapter(
        private val items: List<AppSelectionItem>,
        private val selectedSet: Set<String>,
        private val onToggle: (AppSelectionItem, Boolean) -> Unit
    ) : RecyclerView.Adapter<AppSelectionAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val iconView: ImageView = view.findViewById(R.id.app_setting_icon)
            val labelView: TextView = view.findViewById(R.id.app_setting_label)
            val packageView: TextView = view.findViewById(R.id.app_setting_package)
            val checkBox: CheckBox = view.findViewById(R.id.app_setting_checkbox)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hidden_app_setting, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.iconView.setImageDrawable(item.icon)
            holder.labelView.text = item.label
            holder.packageView.text = item.packageName

            holder.checkBox.setOnCheckedChangeListener(null)
            holder.checkBox.isChecked = selectedSet.contains(item.packageName)

            val toggle = {
                val state = !holder.checkBox.isChecked
                holder.checkBox.isChecked = state
                onToggle(item, state)
            }

            holder.itemView.setOnClickListener { toggle() }
            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                onToggle(item, isChecked)
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
