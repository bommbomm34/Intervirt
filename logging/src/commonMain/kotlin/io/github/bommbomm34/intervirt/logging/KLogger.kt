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
    private val stream: OutputStream = getDefaultStream(),
) {
    constructor(
        name: KClass<*>,
        level: LogLevel,
    ) : this(name.simpleName ?: "", level)

    fun trace(block: Output) {
        if (level.priority == LogLevel.TRACE.priority) {
            block().log("TRACE")
        }
    }

    fun debug(block: Output) {
        if (level.priority <= LogLevel.DEBUG.priority) {
            block().log("DEBUG", LogColor.GREEN)
        }
    }

    fun info(block: Output) {
        if (level.priority <= LogLevel.INFO.priority) {
            block().log("INFO", LogColor.BLUE)
        }
    }

    fun warn(block: Output) {
        if (level.priority <= LogLevel.WARN.priority) {
            block().log("WARN", LogColor.YELLOW)
        }
    }

    fun error(throwable: Throwable? = null, block: Output? = null) {
        if (level.priority <= LogLevel.ERROR.priority) {
            block?.invoke()?.log("ERROR", LogColor.RED, true)
            throwable?.printStackTrace()
        }
    }

    private fun Any?.log(
        prefix: String,
        color: String = LogColor.DEFAULT,
        err: Boolean = false,
    ) {
        val time = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .format(ISO_8601_FORMAT)
        val output = "$time [$prefix] $name - ${toString()}".tryColor(color)
        if (err) stream.printlnErr(output) else stream.println(output)
    }

    private fun String.tryColor(color: String) = if (stream.colorSupported) "$color$this$ANSI_RESET" else this
}

private typealias Output = () -> Any?