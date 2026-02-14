package io.github.bommbomm34.intervirt.core

import kotlinx.serialization.json.Json

const val CURRENT_VERSION = "0.0.1"

val defaultJson = Json {
    ignoreUnknownKeys = true
}