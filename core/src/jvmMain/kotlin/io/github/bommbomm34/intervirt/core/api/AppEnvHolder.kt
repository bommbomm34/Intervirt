package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import kotlin.reflect.KProperty

interface AppEnvHolder {
    fun get(): AppEnv
    infix fun set(new: AppEnv)
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun AppEnvHolder.getValue(ref: Any?, property: KProperty<*>): AppEnv =
    get()
