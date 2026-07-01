/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.bommbomm34.intervirt.core.data.AppEnv
import kotlin.reflect.KProperty0

@Composable
inline fun <T> AppEnv.state(producer: AppEnv.() -> KProperty0<T>): State<T> {
    val property = producer()

    return remember {
        val state = mutableStateOf(property.get())
        addOnChange(property.name) { state.value = property.get() }
        state
    }
}
