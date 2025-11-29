package io.github.bommbomm34.intervirt

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*

const val DEBUG_ENABLED = true

val SUPPORTED_ARCHITECTURES = listOf("amd64", "arm64")
val START_ALPINE_VM_COMMANDS = listOf(
    "./qemu-system",
    "-drive",
    "file=../disk/alpine-linux.qcow2,format=qcow2",
    "-m",
    "2048",
    "-serial",
    "tcp::5555,server,nowait",
    "-monitor",
    "tcp::5555,server,nowait"
)
val preferences = Preferences()
val logger = KotlinLogging.logger {  }
val client = HttpClient(CIO) {
    engine {
        requestTimeout = 0
    }
}