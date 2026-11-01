package com.saggitt.omega.compose.screens.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.BaseDialog
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.preferences.BasePreference
import com.saggitt.omega.compose.components.preferences.IntSelectionPrefDialogUI
import com.saggitt.omega.compose.components.preferences.PreferenceGroup
import com.saggitt.omega.compose.components.preferences.StringMultiSelectionPrefDialogUI
import com.saggitt.omega.compose.components.preferences.StringSelectionPrefDialogUI
import com.saggitt.omega.compose.components.preferences.SwitchPreference
import com.saggitt.omega.preferences.BasePreferences
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.views.IosNavBarView

@Composable
fun IosNavPrefsPage() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    val openDialog = remember { mutableStateOf(false) }
    var dialogPref by remember { mutableStateOf<Any?>(null) }
    val onPrefDialog = { pref: Any ->
        dialogPref = pref
        openDialog.value = true
    }

    val appearancePrefs = listOf(
        prefs.iosNavColorMode,
        prefs.iosNavOpacity,
        prefs.iosNavWidth,
        prefs.iosNavHeight,
        prefs.iosNavBottomMargin,
        prefs.iosNavRadius
    )

    val actionPrefs = listOf(
        prefs.iosNavHaptic,
        prefs.iosNavSwipeUpAction,
        prefs.iosNavSwipeHoldAction,
        prefs.iosNavSwipeSideAction,
        prefs.iosNavDoubleTapAction,
        prefs.iosNavLongPressAction
    )

    OmegaAppTheme {
        ViewWithActionBar(
            title = stringResource(R.string.title_ios_nav_bar)
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            val isDark = isSystemInDarkTheme()
                            val colorMode = prefs.iosNavColorMode.onGetValue()
                            val baseColor = when (colorMode) {
                                "white" -> Color.White
                                "black" -> Color.Black
                                "accent" -> MaterialTheme.colorScheme.primary
                                else -> if (isDark) Color.White else Color.Black
                            }
                            val opacity = prefs.iosNavOpacity.onGetValue().coerceIn(0.1f, 1f)
                            val width = prefs.iosNavWidth.onGetValue().coerceIn(60f, 260f).dp
                            val height = prefs.iosNavHeight.onGetValue().coerceIn(2f, 16f).dp
                            val radius = prefs.iosNavRadius.onGetValue().coerceIn(0f, 20f).dp
                            val bottomMargin = prefs.iosNavBottomMargin.onGetValue().coerceIn(0f, 32f).dp

                            Box(
                                modifier = Modifier
                                    .padding(bottom = bottomMargin + 8.dp)
                                    .width(width)
                                    .height(height)
                                    .clip(RoundedCornerShape(radius))
                                    .background(baseColor.copy(alpha = opacity))
                            )
                        }
                    }
                }

                item {
                    PreferenceGroup(
                        heading = stringResource(R.string.category_ios_nav_system)
                    ) {
                        SwitchPreference(
                            pref = prefs.iosNavEnabled,
                            index = 0,
                            groupSize = 3
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        SwitchPreference(
                            pref = prefs.iosNavHideSystemBar,
                            index = 1,
                            groupSize = 3
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        BasePreference(
                            titleId = R.string.ios_nav_system_settings,
                            summaryId = R.string.ios_nav_system_settings_summary,
                            index = 2,
                            groupSize = 3,
                            onClick = {
                                IosNavBarView.openSystemNavigationSettings(context)
                            }
                        )
                    }
                }

                item {
                    PreferenceGroup(
                        heading = stringResource(R.string.category_ios_nav_appearance),
                        prefs = appearancePrefs,
                        onPrefDialog = onPrefDialog
                    )
                }

                item {
                    PreferenceGroup(
                        heading = stringResource(R.string.category_ios_nav_actions),
                        prefs = actionPrefs,
                        onPrefDialog = onPrefDialog
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (openDialog.value) {
                BaseDialog(openDialogCustom = openDialog) {
                    when (dialogPref) {
                        is BasePreferences.IntSelectionPref -> IntSelectionPrefDialogUI(
                            pref = dialogPref as BasePreferences.IntSelectionPref,
                            openDialogCustom = openDialog
                        )
                        is BasePreferences.StringSelectionPref -> StringSelectionPrefDialogUI(
                            pref = dialogPref as BasePreferences.StringSelectionPref,
                            openDialogCustom = openDialog
                        )
                        is BasePreferences.StringMultiSelectionPref -> StringMultiSelectionPrefDialogUI(
                            pref = dialogPref as BasePreferences.StringMultiSelectionPref,
                            openDialogCustom = openDialog
                        )
                    }
                }
            }
        }
    }
}
