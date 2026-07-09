/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import com.russhwolf.settings.Settings
import io.github.bommbomm34.intervirt.core.api.atomic.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.atomic.Holder
import io.github.bommbomm34.intervirt.core.api.atomic.ProjectHolder
import io.github.bommbomm34.intervirt.core.api.atomic.impl.AppEnvHolder
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.impl.AppEnvHolderImpl
import io.github.bommbomm34.intervirt.impl.ProjectHolderImpl
import io.github.bommbomm34.intervirt.logging.KLogger
import org.koin.compose.koinInject
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.plugin.module.dsl.single
import java.awt.datatransfer.StringSelection

inline val currentAppEnv: AppEnv
    @Composable
    get() = currentAppState.env.value

inline val currentAppEnvHolder: AppEnvHolder
    @Composable
    get() = koinInject()

inline val currentAppState: AppState
    @Composable
    get() = koinInject()

inline val currentProject: State<Project>
    @Composable
    get() = currentAppState.project.collectAsState()

@OptIn(ExperimentalFoundationApi::class)
val PointerMatcher.Companion.Secondary: PointerMatcher
    get() = mouse(PointerButton.Secondary)

@Composable
fun AppEnv.isDarkMode() = darkMode ?: isSystemInDarkTheme()

@Composable
fun rememberLogger(name: String): KLogger {
    val appEnv = currentAppEnv
    return remember { appEnv.getLogger(name) }
}

@OptIn(ExperimentalComposeUiApi::class)
suspend fun Clipboard.copyToClipboard(text: String) {
    setClipEntry(ClipEntry(StringSelection(text)))
}

fun Module.singleAppEnvHolder() = single { AppEnvHolder(AppEnvHolderImpl(get())) }
fun Module.singleProjectHolder() = single { ProjectHolder(ProjectHolderImpl(get())) }

fun Module.singleAppState() = single { AppState(get<Settings>()) }

fun Module.singleTestAppState() = single { AppState(getTestAppEnv()) }
