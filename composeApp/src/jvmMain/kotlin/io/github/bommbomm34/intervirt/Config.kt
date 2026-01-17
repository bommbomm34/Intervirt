package io.github.bommbomm34.intervirt

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowState
import io.github.bommbomm34.intervirt.data.*
import io.github.bommbomm34.intervirt.data.stateful.ViewConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import java.io.File
import java.net.ServerSocket
import java.util.*
import kotlin.math.pow
import kotlin.math.round

const val CURRENT_VERSION = "0.0.1"
const val QEMU_WINDOWS_URL = "https://cdn.perhof.org/bommbomm34/qemu/windows-portable.ziṕ"
const val QEMU_LINUX_URL = "https://cdn.perhof.org/bommbomm34/qemu/linux-portable.zip"
const val ALPINE_DISK_URL = "https://cdn.perhof.org/bommbomm34/intervirt/alpine-disk.qcow2"

val DEBUG_ENABLED = env("DEBUG_ENABLED").toBoolean()
val SSH_TIMEOUT = env("SSH_TIMEOUT")?.toLong() ?: 30000L
val AGENT_PORT = env("AGENT_PORT")?.toInt() ?: 55436
val VM_SHUTDOWN_TIMEOUT = env("VM_SHUTDOWN_TIMEOUT")?.toLong() ?: 30000L
val SUPPORTED_ARCHITECTURES = listOf("amd64", "arm64")
val VM_RAM = env("VM_RAM")?.toInt() ?: 2048
val VM_CPU = env("VM_CPU")?.toInt() ?: 1
val VM_ENABLE_KVM = env("VM_ENABLE_KVM")?.toBoolean() ?: false
val DATA_DIR = File(env("DATA_DIR") ?: (System.getProperty("user.home") + File.separator + "Intervirt"))
val DARK_MODE = env("DARK_MODE")?.toBoolean()
val START_ALPINE_VM_COMMANDS = listOf(
    FileManager.getQEMUFile().absolutePath,
    if (VM_ENABLE_KVM) "-enable-kvm" else "",
    "-smp", VM_CPU.toString(),
    "-drive", "file=../disk/alpine-linux.qcow2,format=qcow2",
    "-m", VM_RAM.toString(),
    "-netdev", "user,id=net0,hostfwd=tcp:127.0.0.1:$AGENT_PORT-:55436,dns=9.9.9.9",
    "-device", "e1000,netdev=net0",
    "-nographic"
)
val AVAILABLE_LANGUAGES = listOf(
    Locale.US
)
val TOOLTIP_FONT_SIZE = 12.sp
val CONNECTION_STROKE_WIDTH = env("CONNECTION_STROKE_WIDTH")?.toFloat() ?: 3f
val DEVICE_CONNECTION_COLOR = env("DEVICE_CONNECTION_COLOR")?.toInt(16) ?: 0x001100
val ZOOM_SPEED = env("ZOOM_SPEED")?.toFloat() ?: 0.1f
val DEVICE_SIZE = env("DEVICE_SIZE")?.toInt()?.dp ?: 100.dp
val OS_ICON_SIZE = env("OS_ICON_SIZE")?.toInt()?.dp ?: 128.dp
val SUGGESTED_FILENAME = env("SUGGESTED_FILENAME") ?: "MyIntervirtProject"
val LANGUAGE: Locale = env("LANGUAGE")?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault() ?: Locale.US
val logger = KotlinLogging.logger { }
val client = HttpClient(CIO) {
    engine {
        requestTimeout = 0
    }
    install(WebSockets)
}
val logs = mutableStateListOf<String>()
var showLogs by mutableStateOf(false)
var dialogState: DialogState by mutableStateOf(DialogState.Default)
var devicesViewZoom by mutableStateOf(1f)
var isCtrlPressed by mutableStateOf(false)
val CURRENT_FILE: PlatformFile? by mutableStateOf(null)
var currentScreenIndex by mutableStateOf(if (checkSetupStatus()) 1 else 0)
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
            image = "debian/13",
            name = "My Debian",
            x = 500,
            y = 500,
            ipv4 = "192.168.0.20",
            ipv6 = "fd00:6767:6767:6767:0808:abcd:abcd:aaaa",
            internetEnabled = false,
            portForwardings = mutableMapOf(
                67 to 25565
            )
        )
    ),
    connections = mutableListOf()
)
val statefulConf = ViewConfiguration(configuration)
var windowState = WindowState(size = DpSize(1200.dp, 1000.dp))

fun env(name: String): String? = System.getenv("INTERVIRT_$name") ?: Preferences.loadString(name)
fun String.versionCode() = replace(".", "").toInt()

fun String.result() = Result.success(this)

fun <T> Exception.result() = Result.failure<T>(this)

fun Float.roundBy(num: Int = 2): Float {
    val factor = 10f.pow(num)
    return round(times(factor)) / factor
}

fun Float.readablePercentage() = "${(times(100f)).roundBy()}%"

fun openDialog(
    importance: Importance,
    message: String
) {
    dialogState = DialogState.Regular(
        importance = importance,
        message = message,
        visible = true
    )
}

fun openDialog(customContent: @Composable () -> Unit) {
    dialogState = DialogState.Custom(customContent, true)
}

fun closeDialog() {
    dialogState = when (dialogState) {
        is DialogState.Custom -> (dialogState as DialogState.Custom).copy(visible = false)
        is DialogState.Regular -> (dialogState as DialogState.Regular).copy(visible = false)
    }
}

@Composable
fun dpToPx(dp: Dp) = with(LocalDensity.current) { dp.toPx() }

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

fun checkSetupStatus() = env("INSTALLED").toBoolean()

fun applyConfiguration(vmConf: VMConfigurationData, appConf: AppConfigurationData) {
    Preferences.saveString("VM_RAM", vmConf.ram.toString())
    Preferences.saveString("VM_CPU", vmConf.cpu.toString())
    Preferences.saveString("VM_ENABLE_KVM", vmConf.kvm.toString())
    Preferences.saveString("VM_SHUTDOWN_TIMEOUT", appConf.vmShutdownTimeout.toString())
    Preferences.saveString("AGENT_PORT", appConf.agentPort.toString())
    Preferences.saveString("DATA_DIR", appConf.intervirtFolder)
    Preferences.saveString("DARK_MODE", appConf.darkMode.toString())
    Preferences.saveString("LANGUAGE", appConf.language)
}

@OptIn(ExperimentalFoundationApi::class)
val PointerMatcher.Companion.Secondary: PointerMatcher
    get() = PointerMatcher.mouse(PointerButton.Secondary)

@Composable fun isDarkMode() = DARK_MODE ?: isSystemInDarkTheme()