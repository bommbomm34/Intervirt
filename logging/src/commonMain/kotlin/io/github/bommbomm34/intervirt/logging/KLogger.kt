/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.logging

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.reflect.KClass
import kotlin.time.Clock

private val ISO_8601_FORMAT = LocalDateTime.Format {
    year()
    char('-')
    monthNumber()
    char('-')
    day()
    char('T')
    hour()
    char(':')
    minute()
    char(':')
    second()
}
private const val ANSI_RESET = "\u001B[0m"

class KLogger(
    val name: String,
    val level: LogLevel,
    vararg streams: OutputStream,
) {
    private val streams = streams.ifEmpty { arrayOf(getDefaultStream()) }

    constructor(
        name: KClass<*>,
        level: LogLevel,
        vararg streams: OutputStream,
    ) : this(name.simpleName ?: "", level, *streams)

    fun log(text: Any?, level: LogLevel) {
        val time = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .format(ISO_8601_FORMAT)
        val output = "$time [${level.name}] $name - $text"
        if (level == LogLevel.ERROR) output.printlnErr(level.color) else output.println(level.color)
    }

    private fun String.println(color: LogColor) =
        streams.forEach { it.println(this.tryColor(color, it.colorSupported)) }

    private fun String.printlnErr(color: LogColor) =
        streams.forEach { it.printlnErr(this.tryColor(color, it.colorSupported)) }

    private fun String.tryColor(color: LogColor, colorSupported: Boolean) =
        if (colorSupported) "$color$this$ANSI_RESET" else this
}

inline fun KLogger.error(throwable: Throwable? = null, block: Output = { "" }) {
    log(LogLevel.ERROR, block) {
        throwable?.printStackTrace()
    }
}

inline fun KLogger.warn(block: Output) {
    log(LogLevel.WARN, block)
}

inline fun KLogger.info(block: Output) {
    log(LogLevel.INFO, block)
}

inline fun KLogger.debug(block: Output) {
    log(LogLevel.DEBUG, block)
}

inline fun KLogger.trace(block: Output) {
    log(LogLevel.TRACE, block)
}

@PublishedApi
internal inline fun KLogger.log(
    level: LogLevel,
    block: Output,
    postLog: () -> Unit = {},
) {
    if (this.level <= level) {
        log(block(), level)
        postLog()
    }
}

private typealias Output = () -> Any?
