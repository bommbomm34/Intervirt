/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.exceptions

class CommandExecutionException(
    statusCode: Int,
    val output: String,
) : Exception("Command failed with status code $statusCode: $output")