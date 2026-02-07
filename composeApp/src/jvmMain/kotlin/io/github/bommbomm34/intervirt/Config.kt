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
import io.github.bommbomm34.intervirt.api.*
import io.github.bommbomm34.intervirt.api.impl.AgentClient
import io.github.bommbomm34.intervirt.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.data.*
import io.github.bommbomm34.intervirt.data.stateful.AppState
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.utils.io.CancellationException
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.binds
import org.koin.dsl.module
import java.net.ServerSocket
import java.util.*
import kotlin.math.pow
import kotlin.math.round

const val CURRENT_VERSION = "0.0.1"
const val HELP_URL = "https://docs.perhof.org/intervirt"
const val HOMEPAGE_URL = "https://perhof.org/intervirt"

val mainModule = module {
    singleOf(::Executor)
    singleOf(::Downloader)
    single {
        (if (get<AppEnv>().pseudoMode) VirtualGuestManager() else AgentClient(get()))
    }.binds(arrayOf(GuestManager::class))
    singleOf(::DeviceManager)
    singleOf(::FileManager)
    singleOf(::Preferences)
    singleOf(::QemuClient)
    singleOf(::AppState)
    single { get<Preferences>().getAppEnv() }
}
val AVAILABLE_LANGUAGES = listOf(
    Locale.US
)
val SUPPORTED_ARCHITECTURES = listOf("amd64", "arm64")
val client = HttpClient(CIO) {
    engine {
        requestTimeout = 0
    }
    install(WebSockets){
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
}
lateinit var density: Density
val configuration = IntervirtConfiguration(
    version = CURRENT_VERSION,
    author = "",
    devices = mutableListOf(
        Device.Switch(
            id = "switch-88888",
            name = "My Switch",
            x = 300,
            y = 300
        ),
        Device.Computer(
            id = "computer-67676",
            image = "intervirtos/1",
            name = "My Debian",
            x = 500,
            y = 500,
            ipv4 = "192.168.0.20",
            ipv6 = "fd00:6767:6767:6767:0808:abcd:abcd:aaaa",
            mac = "fd:67:67:67:67:67",
            internetEnabled = false,
            portForwardings = mutableListOf(
                PortForwarding("tcp", 67, 25565)
            )
        )
    ),
    connections = mutableListOf()
).apply { connections.add(DeviceConnection.SwitchComputer(devices[0].id, devices[1].id)) }
fun String.versionCode() = replace(".", "").toInt()

fun String.result() = Result.success(this)

fun <T> Exception.result() = Result.failure<T>(this)

fun Float.roundBy(num: Int = 2): Float {
    val factor = 10f.pow(num)
    return round(times(factor)) / factor
}

fun Float.readablePercentage() = "${(times(100f)).roundBy()}%"

@Composable
fun dpToPx(dp: Dp) = with(LocalDensity.current) { dp.toPx() }

suspend inline fun <T> runSuspendingCatching(block: suspend () -> T): Result<T> {
    2 + 2
    return try {
        Result.success(block())
    } catch (e: CancellationException){
        throw e
    } catch (e: Throwable){
        Result.failure(e)
    }
}
fun <T> List<T>.addFirst(element: T): List<T> {
    val mutableList = toMutableList()
    mutableList.addFirst(element)
    return mutableList
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
    saveString("VM_SHUTDOWN_TIMEOUT", appConf.vmShutdownTimeout.toString())
    saveString("AGENT_PORT", appConf.agentPort.toString())
    saveString("DATA_DIR", appConf.intervirtFolder)
    saveString("DARK_MODE", appConf.darkMode.toString())
    saveString("LANGUAGE", appConf.language)
}

@OptIn(ExperimentalFoundationApi::class)
val PointerMatcher.Companion.Secondary: PointerMatcher
    get() = PointerMatcher.mouse(PointerButton.Secondary)

@Composable fun AppEnv.isDarkMode() = darkMode ?: isSystemInDarkTheme()

fun Dp.toPx() = density.run { toPx() }

@Composable
fun rememberLogger(name: String) = remember { KotlinLogging.logger(name) }

internal suspend fun Res.readString(path: String) = readBytes(path).decodeToString()