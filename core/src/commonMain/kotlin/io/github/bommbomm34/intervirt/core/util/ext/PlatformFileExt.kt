/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util.ext

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.write
import java.nio.file.Path

suspend fun PlatformFile.createFile() = write(byteArrayOf(0))

fun PlatformFile.toJavaPath(): Path = file.toPath()