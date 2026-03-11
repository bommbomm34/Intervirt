/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import intervirt.ui.generated.resources.Res
import io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.components.dialogs.ProgressDialog
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.ProxyManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerBasedManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.syncProject
import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.bommbomm34.intervirt.core.util.getLogger
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.state
import io.github.bommbomm34.intervirt.data.toViewProject
import io.github.bommbomm34.intervirt.logging.KLogger

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import java.awt.datatransfer.StringSelection
import java.io.File
import java.net.ServerSocket

fun String.versionCode() = replace(".", "").toInt()

fun String.result() = Result.success(this)

fun <T> Exception.result() = Result.failure<T>(this)

@Composable
fun dpToPx(dp: Dp) = with(LocalDensity.current) { dp.toPx() }

suspend inline fun <T> runSuspendingCatching(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

fun Int.isValidPort() = this in 1..65535

fun Int.canPortBind(): Result<Unit> {
    try {
        ServerSocket(this).use {
            return Result.success(Unit)
        }
    } catch (e: Exception) {
        return Result.failure(e)
    }
}

@OptIn(ExperimentalFoundationApi::class)
val PointerMatcher.Companion.Secondary: PointerMatcher
    get() = PointerMatcher.mouse(PointerButton.Secondary)

@Composable
fun AppEnv.isDarkMode() = state { ::DARK_MODE }.value ?: isSystemInDarkTheme()

fun Dp.toPx() = density.run { toPx() }

@Composable
fun <T> IntervirtOSClient.rememberManager(func: (IntervirtOSClient) -> T): T = remember(this) { func(this) }

@Composable
fun <T> IntervirtOSClient.rememberManager(func: (AppEnv, IntervirtOSClient) -> T): T {
    val appEnv = koinInject<AppEnv>()
    return remember { func(appEnv, this) }
}


@Composable
fun rememberLogger(name: String): KLogger {
    val appEnv = koinInject<AppEnv>()
    return remember { appEnv.getLogger(name) }
}

@Composable
fun DockerBasedManager.initialize(): MutableState<Boolean> {
    val appState = koinInject<AppState>()
    val initialized = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    CatchingLaunchedEffect {
        appState.openDialog {
            ProgressDialog(
                flow = init(),
                onClose = ::close,
                onMessage = { progress ->
                    if (progress is ResultProgress.Result) {
                        progress.result.fold(
                            onSuccess = { initialized.value = true },
                            onFailure = {
                                scope.launch {
                                    appState.showExceptionDialog(it)
                                }
                            },
                        )
                    }
                },
            )
        }
    }
    return initialized
}

fun CoroutineScope.initDocker(
    appState: AppState,
    manager: DockerBasedManager,
    onInitialize: () -> Unit,
) {
    launchDialogCatching(appState) {
        appState.openDialog {
            ProgressDialog(
                flow = manager.init(),
                onClose = ::close,
                onMessage = { progress ->
                    if (progress is ResultProgress.Result) {
                        progress.result.fold(
                            onSuccess = { onInitialize() },
                            onFailure = {
                                launch {
                                    appState.showExceptionDialog(it)
                                }
                            },
                        )
                    }
                },
            )
        }
    }
}

@Composable
fun rememberProxyManager(
    appEnv: AppEnv,
    deviceManager: DeviceManager,
    osClient: IntervirtOSClient,
) = remember(osClient) { ProxyManager(appEnv, deviceManager, osClient) }

@OptIn(ExperimentalComposeUiApi::class)
suspend fun Clipboard.copyToClipboard(text: String) {
    setClipEntry(ClipEntry(StringSelection(text)))
}

internal suspend fun Res.readString(path: String) = readBytes(path).decodeToString()

@Composable
fun rememberFileSaverLauncher(onResult: (PlatformFile?) -> Unit) = rememberFileSaverLauncher(
    dialogSettings = FileKitDialogSettings.createDefault(),
    onResult = onResult,
)

fun File.writeConf(project: Project) = writeText(defaultJson.encodeToString(project))

suspend fun File.loadConf(
    project: Project,
    appState: AppState,
    guestManager: GuestManager,
    onComplete: () -> Unit = {},
) {
    val fileContent = readText()
    val newConfiguration = Json.decodeFromString<Project>(fileContent)
    project.update(newConfiguration)
    val flow = guestManager.syncProject(project).onCompletion { onComplete() }
    appState.openDialog {
        ProgressDialog(
            flow = flow,
            onClose = ::close,
        )
    }
    appState.statefulProject.update(newConfiguration.toViewProject())
}