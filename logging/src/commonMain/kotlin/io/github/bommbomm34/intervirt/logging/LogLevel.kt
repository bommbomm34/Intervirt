/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.logging

enum class LogLevel(internal val color: LogColor) {
    TRACE(LogColor.DEFAULT),
    DEBUG(LogColor.GREEN),
    INFO(LogColor.BLUE),
    WARN(LogColor.YELLOW),
    ERROR(LogColor.RED),
}
