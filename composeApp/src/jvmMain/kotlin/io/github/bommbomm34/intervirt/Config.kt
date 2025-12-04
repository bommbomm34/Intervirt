package io.github.bommbomm34.intervirt

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*

const val DEBUG_ENABLED = true
const val SSH_PORT = 2222
const val SSH_TIMEOUT = 30000L
const val VM_SHUTDOWN_TIMEOUT = 30000L

val SUPPORTED_ARCHITECTURES = listOf("amd64", "arm64")
val START_ALPINE_VM_COMMANDS = listOf(
    "qemu-system-x86_64",
    "-drive", "file=../disk/alpine-linux.qcow2,format=qcow2",
    "-m", "2048",
    "-netdev", "user,id=net0,hostfwd=tcp:127.0.0.1:$SSH_PORT-:22,dns=9.9.9.9,dns=1.1.1.1",
    "-device", "e1000,netdev=net0",
    "-nographic"
)
val preferences = Preferences()
val logger = KotlinLogging.logger { }
val client = HttpClient(CIO) {
    engine {
        requestTimeout = 0
    }
}
val guestSession: Session = JSch()
    .getSession("root", "127.0.0.1", SSH_PORT)
    .apply { setConfig("StrictHostKeyChecking", "no") }