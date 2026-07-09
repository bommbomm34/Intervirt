/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.*
import com.russhwolf.settings.Settings
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.terminal_window_title
import io.github.bommbomm34.intervirt.components.DefaultWindowScope
import io.github.bommbomm34.intervirt.components.dialogs.Dialog
import io.github.bommbomm34.intervirt.core.api.FileManager
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.api.ShutdownHandler
import io.github.bommbomm34.intervirt.core.api.atomic.Holder
import io.github.bommbomm34.intervirt.core.api.atomic.ProjectHolder
import io.github.bommbomm34.intervirt.core.coreModule
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.data.env.storeEnv
import io.github.bommbomm34.intervirt.core.util.Atomic
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Screen
import io.github.bommbomm34.intervirt.data.hasIntervirtOS
import io.github.bommbomm34.intervirt.intervirtos.Main
import io.github.bommbomm34.intervirt.util.ext.loadConf
import io.github.bommbomm34.intervirt.util.ext.writeConf
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.exists
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.KoinConfiguration
import java.util.*


fun main() = application {
    KoinApplication(
        configuration = KoinConfiguration {
            modules(coreModule, uiModule, intervirtOSViewModelsModule)
        },
    ) {
        val shutdownHandler = koinInject<ShutdownHandler>()
        val appEnv = currentAppEnv
        val guestManager = koinInject<GuestManager>()
        val appState = koinInject<AppState>()
        val fileManager = koinInject<FileManager>()
        val project = koinInject<ProjectHolder>()
        val settings = koinInject<Settings>()
        val tempConfFile = remember { fileManager.getFile("cache/temp.ivrt") }
        LaunchedEffect(Unit) {
            if (!appEnv.installed) appState.currentScreen = Screen.SETUP
            // Set exception handler
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                shutdownHandler.crash(thread, throwable)
            }
            // These things should be only called once
            Locale.setDefault(Locale.forLanguageTag(appEnv.language))
            FileKit.init("intervirt")
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(
                Thread {
                    runBlocking {
                        settings.storeEnv(appEnv)
                        if (appEnv.enableTempFile) tempConfFile.writeConf(project.get())
                        shutdownHandler.gracefulShutdown()
                    }
                },
            )
            // Load temp file if exists
            if (tempConfFile.exists() && appEnv.enableTempFile) tempConfFile.loadConf(
                project,
                appState,
                guestManager,
            )
        }
        density = LocalDensity.current
        // Main Window
        Window(
            onCloseRequest = {
                exitApplication()
            },
            onKeyEvent = {
                appState.isCtrlPressed = it.isCtrlPressed
                if (it.key == Key.Escape) {
                    if (appState.drawingConnectionSource != null) {
                        appState.drawingConnectionSource = null
                        true
                    } else if (appState.deviceSettingsVisible) {
                        appState.deviceSettingsVisible = false
                        true
                    } else false
                } else false
            },
            state = appState.windowState,
            title = "Intervirt",
        ) {
            DefaultWindowScope(onPointerEvent = { appState.mousePosition = it.changes.first().position }) {
                App()
            }
        }
        // Logs Window
        Window(
            onCloseRequest = { appState.showLogs = false },
            visible = appState.showLogs,
            title = "Intervirt Logs",
            state = rememberWindowState(position = WindowPosition.Aligned(Alignment.CenterEnd)),
        ) {
            DefaultWindowScope {
                LogsView(appState.logs)
            }
        }
        // OS Window
        Window(
            onCloseRequest = { appState.openComputerShell = null },
            visible = appState.openComputerShell != null,
            title = appState.osWindowTitle ?: stringResource(
                Res.string.terminal_window_title,
                appState.openComputerShell?.name ?: "",
            ),
        ) {
            DefaultWindowScope {
                appState.openComputerShell?.let {
                    // Check if device has IntervirtOS installed
                    if (it.hasIntervirtOS()) Main(it) else ShellViewWindow(it)
                }
            }
        }
        // Dialog Windows
        appState.dialogStates.forEach { dialogState ->
            DialogWindow(
                onCloseRequest = dialogState::close,
                title = dialogState.title,
                alwaysOnTop = true,
                state = rememberDialogState(size = dialogState.size),
            ) {
                DefaultWindowScope {
                    Dialog(dialogState)
                }
            }
        }
    }
}
