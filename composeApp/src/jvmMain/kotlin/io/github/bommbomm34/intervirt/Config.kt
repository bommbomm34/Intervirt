package io.github.bommbomm34.intervirt

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import intervirt.composeapp.generated.resources.Res
import io.github.bommbomm34.intervirt.core.api.*
import io.github.bommbomm34.intervirt.core.api.impl.AgentClient
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.*
import io.github.bommbomm34.intervirt.data.AppState
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.binds
import org.koin.dsl.module
import java.net.ServerSocket
import java.util.*
import kotlin.reflect.KFunction

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

fun Preferences.applyConfiguration(vmConf: VMConfigurationData, appConf: AppConfigurationData) {
    saveString("VM_RAM", vmConf.ram.toString())
    saveString("VM_CPU", vmConf.cpu.toString())
    saveString("VM_ENABLE_KVM", vmConf.kvm.toString())
    saveString("VM_DISK_URL", vmConf.diskUrl)
    saveString("VM_DISK_HASH_URL", vmConf.diskHashUrl)
    saveString("VM_SHUTDOWN_TIMEOUT", appConf.vmShutdownTimeout.toString())
    saveString("AGENT_PORT", appConf.agentPort.toString())
    saveString("DATA_DIR", appConf.intervirtFolder)
    saveString("DARK_MODE", appConf.darkMode.toString())
    saveString("LANGUAGE", appConf.language)
}

@OptIn(ExperimentalFoundationApi::class)
val PointerMatcher.Companion.Secondary: PointerMatcher
    get() = PointerMatcher.mouse(PointerButton.Secondary)

@Composable
fun AppEnv.isDarkMode() = darkMode ?: isSystemInDarkTheme()

fun Dp.toPx() = density.run { toPx() }

@Composable
fun <T> ContainerClientBundle.rememberClient(func: KFunction<T>): T = remember { func.call(this) }

@Composable
fun rememberLogger(name: String) = remember { KotlinLogging.logger(name) }

internal suspend fun Res.readString(path: String) = readBytes(path).decodeToString()