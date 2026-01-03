package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.logger
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

object Preferences {
    val data = mutableMapOf<String, String>()
    val dataFile: Path = File(System.getProperty("user.home") + File.separator + ".intervirt.config.json").toPath()

    init { load() }

    fun loadString(key: String): String? = data[key]

    fun saveString(key: String, value: String) {
        logger.debug { "Saving string $key with $value" }
        data[key] = value
        flush()
    }

    private fun flush() = Files.writeString(dataFile, Json.encodeToString(data))

    private fun load() {
        if (dataFile.exists()){
            data.clear()
            data.putAll(Json.decodeFromString(Files.readString(dataFile)))
        }
    }
}