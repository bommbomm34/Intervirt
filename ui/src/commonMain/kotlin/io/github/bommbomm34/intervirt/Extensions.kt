/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import io.github.bommbomm34.intervirt.core.api.AppEnvUpdater
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.util.Atomic
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.impl.AppEnvUpdaterImpl
import io.github.bommbomm34.intervirt.logging.KLogger
import org.koin.compose.koinInject
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.plugin.module.dsl.single
import java.awt.datatransfer.StringSelection

inline val currentAppEnv: AppEnv
    @Composable
    get() = currentAppState.env

inline val currentAppState: AppState
    @Composable
    get() = koinInject()

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

fun Module.singleAppEnvUpdater() = single<AppEnvUpdaterImpl>() bind AppEnvUpdater::class

fun Module.singleAppEnv() = single { get<AppState>().env }
