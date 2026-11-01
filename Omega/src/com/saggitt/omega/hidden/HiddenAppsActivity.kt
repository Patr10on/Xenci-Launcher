package com.saggitt.omega.hidden

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R

class HiddenAppsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private lateinit var contentLayout: View
    private val appList = ArrayList<HiddenAppItem>()
    private lateinit var adapter: HiddenAppsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes.blurBehindRadius = 80
        }

        setContentView(R.layout.activity_xiaomi_hidden_apps)

        contentLayout = findViewById(R.id.hidden_content_layout)
        recyclerView = findViewById(R.id.hidden_apps_recycler)
        emptyTextView = findViewById(R.id.empty_state_text)

        findViewById<View>(R.id.btn_manage_hidden_apps).setOnClickListener {
            startActivity(Intent(this, HiddenAppsSettingsActivity::class.java))
        }

        findViewById<View>(R.id.btn_close_hidden_apps).setOnClickListener {
            finish()
        }

        recyclerView.layoutManager = GridLayoutManager(this, 4)
        adapter = HiddenAppsAdapter(appList) { item ->
            val launchIntent = packageManager.getLaunchIntentForPackage(item.packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
                finish()
            }
        }
        recyclerView.adapter = adapter

        contentLayout.visibility = View.INVISIBLE

        com.saggitt.omega.security.AppLockManager.authenticate(
            context = this,
            title = "Gizli Uygulamalar",
            onSuccess = {
                contentLayout.visibility = View.VISIBLE
                loadHiddenApps()
            },
            onCancel = {
                finish()
            }
        )
    }

    override fun onResume() {
        super.onResume()
        if (contentLayout.visibility == View.VISIBLE) {
            loadHiddenApps()
        }
    }

    private fun loadHiddenApps() {
        appList.clear()
        val pm = packageManager
        val hiddenPackages = HiddenAppsManager.getHiddenPackages(this)

        for (pkg in hiddenPackages) {
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val label = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(appInfo)
                appList.add(HiddenAppItem(label, pkg, icon))
            } catch (_: PackageManager.NameNotFoundException) {
            }
        }

        adapter.notifyDataSetChanged()
        emptyTextView.visibility = if (appList.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (appList.isEmpty()) View.GONE else View.VISIBLE
    }

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, HiddenAppsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(intent)
        }
    }

    data class HiddenAppItem(
        val label: String,
        val packageName: String,
        val icon: Drawable
    )

    class HiddenAppsAdapter(
        private val items: List<HiddenAppItem>,
        private val onItemClick: (HiddenAppItem) -> Unit
    ) : RecyclerView.Adapter<HiddenAppsAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val iconView: ImageView = view.findViewById(R.id.hidden_app_icon)
            val labelView: TextView = view.findViewById(R.id.hidden_app_label)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hidden_app, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.iconView.setImageDrawable(item.icon)
            holder.labelView.text = item.label
            holder.itemView.setOnClickListener { onItemClick(item) }
        }

        override fun getItemCount(): Int = items.size
    }
}
