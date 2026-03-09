/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@OptIn(ExperimentalAtomicApi::class)
fun <T, V> atomic(initial: V): ReadWriteProperty<T, V> = object : ReadWriteProperty<T, V> {
    private val ref = AtomicReference(initial)

    override fun getValue(thisRef: T, property: KProperty<*>) = ref.load()

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) = ref.store(value)
}