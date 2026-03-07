/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.exceptions

class UnsupportedArchitectureException : Exception("Architecture ${System.getProperty("os.arch")} is not supported")