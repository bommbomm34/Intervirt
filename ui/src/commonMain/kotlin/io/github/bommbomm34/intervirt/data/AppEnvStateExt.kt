package io.github.bommbomm34.intervirt.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.bommbomm34.intervirt.core.data.AppEnv
import kotlin.reflect.KProperty0

private val propertyStates = mutableMapOf<KProperty0<*>, State<*>>()

@Suppress("UNCHECKED_CAST")
@Composable
fun <T> AppEnv.state(producer: AppEnv.() -> KProperty0<T>): State<T> = remember {
    val property = producer()
    propertyStates[property]?.let { return@remember it as State<T> }
    val state = mutableStateOf(property.get())
    addOnChange(property.name) { state.value = property.get() }
    propertyStates[property] = state
    state
}