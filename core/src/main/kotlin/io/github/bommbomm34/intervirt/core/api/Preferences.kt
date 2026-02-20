package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class Preferences {
    private val logger = KotlinLogging.logger { }
    private val data = mutableMapOf<String, String>()
    private val dataFile: Path =
        File(System.getProperty("user.home") + File.separator + ".intervirt.config.json").toPath()

    init {
        load()
    }

    fun loadString(key: String): String? = data[key]

    fun saveString(key: String, value: String) {
        logger.debug { "Saving string $key with $value" }
        data[key] = value
        flush()
    }

    fun removeString(key: String) {
        logger.debug { "Removing string $key" }
        data.remove(key)
        flush()
    }

    fun getAppEnv() = AppEnv(::env, ::saveString, ::removeString)

    fun env(name: String): String? = System.getenv("INTERVIRT_$name") ?: loadString(name)

    private fun flush() = Files.writeString(dataFile, defaultJson.encodeToString(data))

    private fun load() {
        if (dataFile.exists()) {
            data.clear()
            data.putAll(defaultJson.decodeFromString(Files.readString(dataFile)))
        }
    }
}