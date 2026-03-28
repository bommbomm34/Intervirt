/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.exceptions

class ZipExtractionException(
    val filename: String,
    val errorMessage: String,
) : Exception("Error during ZIP extraction of $filename: $errorMessage")