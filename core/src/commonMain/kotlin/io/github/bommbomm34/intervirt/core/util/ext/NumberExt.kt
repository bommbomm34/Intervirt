/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util.ext

import kotlin.math.pow
import kotlin.math.round

fun Float.readablePercentage() = "${(times(100f)).roundBy()}%"
fun Float.roundBy(num: Int = 2): Float {
    val factor = 10f.pow(num)
    return round(times(factor)) / factor
}