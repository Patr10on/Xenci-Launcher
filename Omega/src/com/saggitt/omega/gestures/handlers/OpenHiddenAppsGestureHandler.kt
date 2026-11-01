package com.saggitt.omega.gestures.handlers

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import com.android.launcher3.R
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.GestureHandler
import com.saggitt.omega.hidden.HiddenAppsActivity
import org.json.JSONObject

class OpenHiddenAppsGestureHandler(context: Context, config: JSONObject?) : GestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.hidden_apps_title)
    override val displayNameRes: Int = R.string.hidden_apps_title
    override val icon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_hidden_locked)

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        HiddenAppsActivity.start(controller.launcher)
    }
}
