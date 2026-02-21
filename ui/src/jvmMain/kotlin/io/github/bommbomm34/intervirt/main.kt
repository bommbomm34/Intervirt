package io.github.bommbomm34.intervirt

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.terminal_window_title
import io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.components.DefaultWindowScope
import io.github.bommbomm34.intervirt.components.dialogs.Dialog
import io.github.bommbomm34.intervirt.core.api.*
import io.github.bommbomm34.intervirt.core.coreModule
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.hasIntervirtOS
import io.github.bommbomm34.intervirt.intervirtos.Main
import io.github.vinceglb.filekit.FileKit
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import java.util.*
import kotlin.system.exitProcess

fun main() = application {
    KoinApplication(
        application = {
            modules(coreModule, uiModule)
        },
    ) {
        val appEnv = koinInject<AppEnv>()
        val deviceManager = koinInject<DeviceManager>()
        val guestManager = koinInject<GuestManager>()
        val qemuClient = koinInject<QemuClient>()
        val httpClient = koinInject<HttpClient>()
        val appState = koinInject<AppState>()
        if (!appEnv.INTERVIRT_INSTALLED) appState.currentScreenIndex = 1
        LaunchedEffect(Unit) {
            // These things should be only called once
            Locale.setDefault(appEnv.LANGUAGE)
            FileKit.init("intervirt")
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(
                Thread {
                    runBlocking {
                        deviceManager.close()
                        guestManager.close()
                        qemuClient.close()
                        httpClient.close()
                    }
                },
            )
            setDefaultExceptionHandler()
        }
        // Initialize SecretProvider
        CatchingLaunchedEffect {
            SecretProvider.init().getOrThrow()
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
            ) {
                DefaultWindowScope {
                    Dialog(dialogState)
                }
            }
        }
    }
}

private fun setDefaultExceptionHandler() {
    Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
        System.err.println("UNCAUGHT EXCEPTION: ${throwable.stackTraceToString()}")
        exitProcess(1)
    }
}