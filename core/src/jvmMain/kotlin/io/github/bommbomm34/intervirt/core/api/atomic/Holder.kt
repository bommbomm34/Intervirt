package io.github.bommbomm34.intervirt.core.api.atomic

import arrow.optics.Lens
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.data.devices
import io.github.bommbomm34.intervirt.core.modify
import io.github.bommbomm34.intervirt.core.util.Atomic
import kotlin.reflect.KProperty

interface Holder<T> {
    fun get(): T

    infix fun set(new: T)

    fun compareAndSet(expected: T, new: T): Boolean
}

inline fun <T> Holder<T>.update(producer: (T) -> T): T {
    while (true) {
        val expected = get()
        val new = producer(expected)

        if (compareAndSet(expected, new)) return new
    }
}

fun <S, A> Lens<S, A>.modify(holder: Holder<S>, producer: (A) -> A): S {
    return holder.update { value ->
        modify(value, producer)
    }
}

operator fun <T> Holder<T>.getValue(thisRef: Any?, property: KProperty<*>): T = get()

operator fun <T> Holder<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    set(value)
}

fun <T : Device> ProjectHolder.modifyDevice(
    device: T,
    action: (T) -> T,
): Project = Project.devices.modify(this) { devices ->
    devices.map { if (it.id == device.id) action(device) else it }
}
