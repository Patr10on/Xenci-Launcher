/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.compose.screens.preferences

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.compose.components.OverflowMenu
import com.saggitt.omega.compose.components.SearchTextField
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.util.appsList

@Composable
fun AppSelectionPage(
    pageTitle: String,
    selectedApps: Set<String>,
    pluralTitleId: Int,
    onSave: (Set<String>) -> Unit
) {
    val colors = CheckboxDefaults.colors(
        checkedColor = MaterialTheme.colorScheme.primary,
        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    var selected by remember { mutableStateOf(selectedApps) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var title by remember { mutableStateOf(pageTitle) }
    val allApps by appsList(comparator = hiddenAppsComparator(selectedApps))
    val filteredApps = remember(allApps, searchQuery) {
        if (searchQuery.isBlank()) {
            allApps
        } else {
            allApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    val pluralTitle = stringResource(id = pluralTitleId, selected.size)
    title = if (selected.isNotEmpty()) {
        pluralTitle
    } else {
        pageTitle
    }

    ViewWithActionBar(
        title = title,
        actions = {
            OverflowMenu {
                DropdownMenuItem(
                    onClick = {
                        selected = emptySet()
                        hideMenu()
                    },
                    text = { Text(text = stringResource(id = R.string.app_reset)) }
                )
            }
        },
        onBackAction = {
            onSave(selected)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                SearchTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.all_apps_search_bar_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps, key = { it.key.toString() }) { app ->
                    val isSelected = rememberSaveable(selected) {
                        mutableStateOf(selected.contains(app.key.toString()))
                    }
                    ListItemWithIcon(
                        title = app.label,
                        modifier = Modifier.clickable {
                            selected = if (isSelected.value) selected.minus(app.key.toString())
                            else selected.plus(app.key.toString())
                        },
                        summary = "",
                        startIcon = {
                            Image(
                                painter = BitmapPainter(app.icon.asImageBitmap()),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        endCheckbox = {
                            Checkbox(
                                checked = isSelected.value,
                                onCheckedChange = {
                                    selected = if (it) selected.plus(app.key.toString())
                                    else selected.minus(app.key.toString())
                                },
                                colors = colors
                            )
                        },
                        horizontalPadding = 4.dp,
                        verticalPadding = 4.dp
                    )
                }
            }
        }
    }

    DisposableEffect(key1 = null) {
        onDispose {
            onSave(selected)
        }
    }
}
