package io.github.bommbomm34.intervirt

import io.github.bommbomm34.intervirt.data.FileManagement
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import java.io.File

val SUPPORTED_ARCHITECTURES = listOf("amd64", "arm64")
val preferences = Preferences()
val logger = KotlinLogging.logger {  }
val client = HttpClient(CIO)