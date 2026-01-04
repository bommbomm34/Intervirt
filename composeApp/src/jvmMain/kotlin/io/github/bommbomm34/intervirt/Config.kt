package io.github.bommbomm34.intervirt

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.sp
import io.github.bommbomm34.intervirt.data.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import java.io.File
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
val TOOLTIP_FONT_SIZE = 12.sp
val CONNECTION_STROKE_WIDTH = env("CONNECTION_STROKE_WIDTH")?.toFloat() ?: 3f
val DEVICE_CONNECTION_COLOR = env("DEVICE_CONNECTION_COLOR")?.toInt(16) ?: 0x001100
val ZOOM_SPEED = env("ZOOM_SPEED")?.toFloat() ?: 0.1f
val logger = KotlinLogging.logger { }
val client = HttpClient(CIO) {
    engine {
        requestTimeout = 0
    }
    install(WebSockets)
}
val logs = mutableStateListOf<String>()
var showLogs by mutableStateOf(false)
var dialogState by mutableStateOf(DialogState.Default)
var devicesViewZoom by mutableStateOf(1f)
val configuration = IntervirtConfiguration(CURRENT_VERSION, "", mutableListOf(), mutableListOf())

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
    dialogState = DialogState(
        importance = importance,
        message = message,
        visible = true
    )
}

fun KeyEvent.isKeyPressed(key: Key) = this.key == key && type == KeyEventType.KeyDown