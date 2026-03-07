package io.github.bommbomm34.intervirt.core.util

import io.github.bommbomm34.intervirt.core.data.AppEnv
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
    val severity: LoggerSeverity,
) {
    companion object {
        val UNKNOWN_LOGGER = KLogger("Unknown", LoggerSeverity.ERROR)
    }

    fun trace(block: Output) {
        if (severity.priority == LoggerSeverity.TRACE.priority) {
            block().log("TRACE")
        }
    }

    fun debug(block: Output) {
        if (severity.priority <= LoggerSeverity.DEBUG.priority) {
            block().log("DEBUG", LoggerColor.GREEN)
        }
    }

    fun info(block: Output) {
        if (severity.priority <= LoggerSeverity.INFO.priority) {
            block().log("INFO", LoggerColor.BLUE)
        }
    }

    fun warn(block: Output) {
        if (severity.priority <= LoggerSeverity.WARN.priority) {
            block().log("WARN", LoggerColor.YELLOW)
        }
    }

    fun error(throwable: Throwable? = null, block: Output? = null) {
        if (severity.priority <= LoggerSeverity.ERROR.priority) {
            block?.invoke()?.log("ERROR", LoggerColor.RED, true)
            throwable?.printStackTrace()
        }
    }

    private fun Any?.log(
        prefix: String,
        color: String = LoggerColor.DEFAULT,
        stderr: Boolean = false,
    ) {
        val time = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .format(ISO_8601_FORMAT)
        val output = "$color$time [$prefix] $name - ${toString()}$ANSI_RESET"
        if (stderr) System.err.println(output) else println(output)
    }
}

enum class LoggerSeverity(val priority: Int) {
    TRACE(0),
    DEBUG(1),
    INFO(2),
    WARN(3),
    ERROR(4),
}

object LoggerColor {
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val DEFAULT = ""
}

fun AppEnv.getLogger(clazz: KClass<*>, vararg suffix: String): KLogger {
    val clazzName = clazz.simpleName ?: ""
    val joined = suffix
        .ifEmpty { null }
        ?.joinToString()
    return getLogger(joined?.let { "$clazzName ($it)" } ?: clazzName)
}

fun AppEnv.getLogger(name: String) = KLogger(
    name = name,
    severity = if (DEBUG_ENABLED) LoggerSeverity.DEBUG else LoggerSeverity.ERROR,
)

private typealias Output = () -> Any?