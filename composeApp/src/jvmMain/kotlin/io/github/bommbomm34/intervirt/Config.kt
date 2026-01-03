package io.github.bommbomm34.intervirt

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import io.github.bommbomm34.intervirt.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.data.Preferences
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
    "qemu-system-x86_64",
    if (VM_ENABLE_KVM) "-enable-kvm" else "",
    "-smp", VM_CPU.toString(),
    "-drive", "file=../disk/alpine-linux.qcow2,format=qcow2",
    "-m", VM_RAM.toString(),
    "-netdev", "user,id=net0,hostfwd=tcp:127.0.0.1:$AGENT_PORT-:55436,dns=9.9.9.9",
    "-device", "e1000,netdev=net0",
    "-nographic"
)
val TOOLTIP_FONT_SIZE = 12.sp
val logger = KotlinLogging.logger { }
val client = HttpClient(CIO) {
    engine {
        requestTimeout = 0
    }
    install(WebSockets)
}
val logs = mutableStateListOf<String>()
var showLogs by mutableStateOf(false)
val configuration = IntervirtConfiguration(CURRENT_VERSION, "", mutableListOf(), mutableListOf())

fun env(name: String): String? = System.getenv("INTERVIRT_$name") ?: Preferences.loadString(name)
fun String.versionCode() = replace(".", "").toInt()

fun String.result() = Result.success(this)

fun <T> Exception.result() = Result.failure<T>(this)

fun Float.roundBy(num: Int = 2): Float {
    val factor = 10f.pow(num)
    return round(times(factor)) / factor
}