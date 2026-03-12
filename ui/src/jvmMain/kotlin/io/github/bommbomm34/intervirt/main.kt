/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.*
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.terminal_window_title
import io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.components.DefaultWindowScope
import io.github.bommbomm34.intervirt.components.dialogs.Dialog
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.FileManager
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.api.QemuClient
import io.github.bommbomm34.intervirt.core.coreModule
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.gracefulShutdown
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.getImages
import io.github.bommbomm34.intervirt.data.hasIntervirtOS
import io.github.bommbomm34.intervirt.intervirtos.Main
import io.github.bommbomm34.intervirt.secret.SecretService
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.exists
import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.KoinConfiguration
import java.util.*
import kotlin.system.exitProcess


fun main() = application {
    KoinApplication(
        configuration = KoinConfiguration {
            modules(coreModule, uiModule, intervirtOSViewModelsModule)
        },
    ) {
        val appEnv = koinInject<AppEnv>()
        val deviceManager = koinInject<DeviceManager>()
        val guestManager = koinInject<GuestManager>()
        val qemuClient = koinInject<QemuClient>()
        val httpClient = koinInject<HttpClient>()
        val secretService = koinInject<SecretService>()
        val appState = koinInject<AppState>()
        val fileManager = koinInject<FileManager>()
        val project = koinInject<Project>()
        val tempConfFile = remember { fileManager.getFile("cache/temp.ivrt") }
        if (!appEnv.INSTALLED) appState.currentScreenIndex = 0
        LaunchedEffect(Unit) {
            // These things should be only called once
            Locale.setDefault(appEnv.LANGUAGE)
            FileKit.init("intervirt")
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(
                Thread {
                    runBlocking {
                        if (appEnv.ENABLE_TEMP_FILE) tempConfFile.writeConf(project)
                        gracefulShutdown(deviceManager, guestManager, qemuClient, httpClient, secretService)
                    }
                },
            )
            setDefaultExceptionHandler()
            // Load temp file if exists
            if (tempConfFile.exists() && appEnv.ENABLE_TEMP_FILE) tempConfFile.loadConf(
                project,
                appState,
                guestManager,
            )
        }
        CatchingLaunchedEffect {
            appState.images.clear()
            appState.images.addAll(httpClient.getImages(appEnv.IMAGES_URL).getOrThrow())
        }
        density = LocalDensity.current
        key(appState.appEnvChangeKey) {
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
}

fun setDefaultExceptionHandler() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        System.err.println(
            """
An unexpected error occurred. This seems like an internal error.
It is recommended to report this error: https://github.com/bommbomm34/Intervirt/issues
Please send the full stacktrace and some system information. Don't send personal data!

Thread: ${thread.name}
Stacktrace:
${throwable.stackTraceToString()}
""".trimIndent(),
        )
        exitProcess(1)
    }
}