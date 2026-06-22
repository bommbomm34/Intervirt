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

    inline fun trace(block: Output) {
        if (level.priority == LogLevel.TRACE.priority) {
            block().log("TRACE")
        }
    }

    inline fun debug(block: Output) {
        if (level.priority <= LogLevel.DEBUG.priority) {
            block().log("DEBUG", LogColor.GREEN)
        }
    }

    inline fun info(block: Output) {
        if (level.priority <= LogLevel.INFO.priority) {
            block().log("INFO", LogColor.BLUE)
        }
    }

    inline fun warn(block: Output) {
        if (level.priority <= LogLevel.WARN.priority) {
            block().log("WARN", LogColor.YELLOW)
        }
    }

    inline fun error(throwable: Throwable? = null, block: Output = { "" }) {
        if (level.priority <= LogLevel.ERROR.priority) {
            block().log("ERROR", LogColor.RED, err = true)
            throwable?.printStackTrace()
        }
    }

    @PublishedApi
    internal fun Any?.log(
        prefix: String,
        color: String = LogColor.DEFAULT,
        err: Boolean = false,
    ) {
        val time = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .format(ISO_8601_FORMAT)
        val output = "$time [$prefix] $name - ${toString()}"
        if (err) output.printlnErr(color) else output.println(color)
    }

    private fun String.println(color: String) =
        streams.forEach { it.println(this.tryColor(color, it.colorSupported)) }

    private fun String.printlnErr(color: String) =
        streams.forEach { it.printlnErr(this.tryColor(color, it.colorSupported)) }

    private fun String.tryColor(color: String, colorSupported: Boolean) = if (colorSupported) "$color$this$ANSI_RESET" else this
}

private typealias Output = () -> Any?
