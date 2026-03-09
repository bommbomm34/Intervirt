/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Serializable(with = AtomicSerializer::class)
@OptIn(ExperimentalAtomicApi::class)
class Atomic<T>(initial: T){
    private val ref = AtomicReference(initial)

    fun set(value: T) = ref.store(value)

    fun get() = ref.load()

    fun update(block: (T) -> T) = ref.update(block)

    inline fun <reified T> getSerializer() = serializer<T>()
}

class AtomicSerializer<T>(private val delegate: KSerializer<T>) : KSerializer<Atomic<T>> {
    override val descriptor = SerialDescriptor("intervirt-atomic", delegate.descriptor)

    override fun serialize(encoder: Encoder, value: Atomic<T>) {
        delegate.serialize(encoder, value.get())
    }

    override fun deserialize(decoder: Decoder): Atomic<T> {
        return Atomic(delegate.deserialize(decoder))
    }
}

@OptIn(ExperimentalAtomicApi::class)
fun <T, V> atomic(
    initial: V,
    onSet: (V) -> Unit = {},
): ReadWriteProperty<T, V> = object : ReadWriteProperty<T, V> {
    private val ref = AtomicReference(initial)

    override fun getValue(thisRef: T, property: KProperty<*>) = ref.load()

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        ref.store(value)
        onSet(value)
    }
}

fun <T> T.toAtomic() = Atomic(this)


fun Atomic<Int>.plus(other: Int) = update { it + other }

fun Atomic<Int>.minus(other: Int) = update { it - other }