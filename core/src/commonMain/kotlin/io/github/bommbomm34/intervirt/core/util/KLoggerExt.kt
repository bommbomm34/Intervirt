/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util

import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.logging.KLogger
import io.github.bommbomm34.intervirt.logging.LogLevel
import kotlin.reflect.KClass

fun AppEnv.getLogger(clazz: KClass<*>, vararg suffix: String): KLogger {
    val clazzName = clazz.simpleName ?: ""
    val joined = suffix
        .ifEmpty { null }
        ?.joinToString()
    return getLogger(joined?.let { "$clazzName ($it)" } ?: clazzName)
}

fun AppEnv.getLogger(name: String) = KLogger(
    name = name,
    level = LOG_LEVEL,
)