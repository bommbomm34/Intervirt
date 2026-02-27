package io.github.bommbomm34.intervirt

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import intervirt.ui.generated.resources.Res
import io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.components.dialogs.ProgressDialog
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.ProxyManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerBasedManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.state
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.ktor.utils.io.*
import org.koin.compose.koinInject
import java.awt.datatransfer.StringSelection
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
fun <T> IntervirtOSClient.rememberManager(func: (IntervirtOSClient) -> T): T = remember { func(this) }

@Composable
fun <T> IntervirtOSClient.rememberManager(func: (AppEnv, IntervirtOSClient) -> T): T {
    val appEnv = koinInject<AppEnv>()
    return remember { func(appEnv, this) }
}


@Composable
fun rememberLogger(name: String) = remember { KotlinLogging.logger(name) }

@Composable
fun DockerBasedManager.initialize(): MutableState<Boolean> {
    val appState = koinInject<AppState>()
    val initialized = remember { mutableStateOf(false) }
    CatchingLaunchedEffect {
        appState.openDialog {
            ProgressDialog(
                flow = init(),
                onClose = ::close,
            )
        }
        initialized.value = true
    }
    return initialized
}

@Composable
fun rememberProxyManager(
    appEnv: AppEnv,
    deviceManager: DeviceManager,
    osClient: IntervirtOSClient,
) = remember { ProxyManager(appEnv, deviceManager, osClient) }

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