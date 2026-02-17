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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import intervirt.ui.generated.resources.Res
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.api.Preferences
import io.github.bommbomm34.intervirt.core.api.intervirtos.ProxyManager
import io.github.bommbomm34.intervirt.core.data.AppConfigurationData
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.VMConfigurationData
import io.github.bommbomm34.intervirt.data.AppState
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.utils.io.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.awt.datatransfer.StringSelection
import java.io.File
import java.net.ServerSocket
import java.util.*

const val CURRENT_VERSION = "0.0.1"
const val HELP_URL = "https://docs.perhof.org/intervirt"
const val HOMEPAGE_URL = "https://perhof.org/intervirt"

val uiModule = module {
    singleOf(::AppState)
}

val AVAILABLE_LANGUAGES = listOf(
    Locale.US
)
val SUPPORTED_ARCHITECTURES = listOf("amd64", "arm64")
lateinit var density: Density
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

fun Preferences.checkSetupStatus() = env("INSTALLED").toBoolean()

fun AppEnv.applyConfiguration(vmConf: VMConfigurationData, appConf: AppConfigurationData) {
    vmRam = vmConf.ram
    vmCpu = vmConf.cpu
    vmEnableKvm = vmConf.kvm
    vmDiskUrl = vmConf.diskUrl
    vmDiskHashUrl = vmConf.diskHashUrl
    vmShutdownTimeout = appConf.vmShutdownTimeout.toLong()
    agentPort = appConf.agentPort
    dataDir = File(appConf.intervirtFolder)
    darkMode = appConf.darkMode
    language = Locale.forLanguageTag(appConf.language)
}

@OptIn(ExperimentalFoundationApi::class)
val PointerMatcher.Companion.Secondary: PointerMatcher
    get() = PointerMatcher.mouse(PointerButton.Secondary)

@Composable
fun AppEnv.isDarkMode() = darkMode ?: isSystemInDarkTheme()

fun Dp.toPx() = density.run { toPx() }

@Composable
fun <T> IntervirtOSClient.rememberManager(func: (IntervirtOSClient) -> T): T = remember { func(this) }

@Composable
fun rememberLogger(name: String) = remember { KotlinLogging.logger(name) }

@Composable
fun rememberProxyManager(
    appEnv: AppEnv,
    deviceManager: DeviceManager,
    osClient: IntervirtOSClient
) = remember { ProxyManager(appEnv, deviceManager, osClient) }

@OptIn(ExperimentalComposeUiApi::class)
suspend fun Clipboard.copyToClipboard(text: String) {
    setClipEntry(ClipEntry(StringSelection(text)))
}

internal suspend fun Res.readString(path: String) = readBytes(path).decodeToString()