/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.util.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import java.net.ServerSocket

@Composable
fun Dp.toPx() = toPx(LocalDensity.current)

fun Dp.toPx(density: Density) = density.run { toPx() }

fun Int.canPortBind(): Result<Unit> {
    try {
        ServerSocket(this).use {
            return Result.success(Unit)
        }
    } catch (e: Exception) {
        return Result.failure(e)
    }
}
