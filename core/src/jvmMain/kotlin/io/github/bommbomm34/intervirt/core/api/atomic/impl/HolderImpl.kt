package io.github.bommbomm34.intervirt.core.api.atomic.impl

import io.github.bommbomm34.intervirt.core.api.atomic.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.atomic.Holder
import io.github.bommbomm34.intervirt.core.api.atomic.ProjectHolder
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.util.Atomic

class HolderImpl<T>(initial: T) : Holder<T> {
    private val atomic = Atomic(initial)

    override fun get(): T {
        return atomic.get()
    }

    override fun set(new: T) {
        atomic.set(new)
    }

    override fun compareAndSet(expected: T, new: T): Boolean {
        return atomic.compareAndSet(expected, new)
    }
}

fun <T> Holder(initial: T): Holder<T> = HolderImpl(initial)

fun AppEnvHolder(initial: AppEnv): AppEnvHolder = AppEnvHolder(Holder(initial))

fun ProjectHolder(initial: Project): ProjectHolder = ProjectHolder(Holder(initial))
