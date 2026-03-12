/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util.ext

fun <T> List<T>.addFirst(element: T): List<T> {
    val mutableList = toMutableList()
    mutableList.addFirst(element)
    return mutableList
}

fun <T> List<T>.patch(element1: T, element2: T) = map { if (it == element1) element2 else it }
