package io.github.bommbomm34.intervirt

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.WebSockets

const val CURRENT_VERSION = "0.0.1"

val DEBUG_ENABLED = env("DEBUG_ENABLED").toBoolean()
val SSH_TIMEOUT = env("SSH_TIMEOUT")?.toLong() ?: 30000L
val AGENT_PORT = env("AGENT_PORT")?.toInt() ?: 55436
val SSH_PORT = env("SSH_PORT")?.toInt() ?: 2222
val VM_SHUTDOWN_TIMEOUT = env("VM_SHUTDOWN_TIMEOUT")?.toLong() ?: 30000L
val SUPPORTED_ARCHITECTURES = listOf("amd64", "arm64")
val START_ALPINE_VM_COMMANDS = listOf(
    "qemu-system-x86_64",
    "-drive", "file=../disk/alpine-linux.qcow2,format=qcow2",
    "-m", "2048",
    "-netdev", "user,id=net0,hostfwd=tcp:127.0.0.1:$AGENT_PORT-:55436,hostfwd=tcp:127.0.0.1:$SSH_PORT-:22,dns=9.9.9.9",
    "-device", "e1000,netdev=net0",
    "-nographic"
)
val preferences = Preferences()
val logger = KotlinLogging.logger { }
val client = HttpClient(CIO) {
    engine {
        requestTimeout = 0
    }
    install(WebSockets)
}
val guestSession: Session = JSch()
    .getSession("root", "127.0.0.1", SSH_PORT)
    .apply { setConfig("StrictHostKeyChecking", "no") }

fun env(name: String): String? = System.getenv("INTERVIRT_$name")