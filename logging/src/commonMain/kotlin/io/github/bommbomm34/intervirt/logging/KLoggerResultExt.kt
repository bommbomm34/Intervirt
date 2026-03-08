/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.logging

fun <T> Result<T>.logOnFailure(logger: KLogger, failureMessage: () -> String) = onFailure {
    logger.error(it, failureMessage)
}