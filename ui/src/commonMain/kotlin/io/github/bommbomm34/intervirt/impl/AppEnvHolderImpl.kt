package io.github.bommbomm34.intervirt.impl

import io.github.bommbomm34.intervirt.core.api.atomic.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.atomic.Holder
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.data.AppState

class AppEnvHolderImpl(private val appState: AppState) : Holder<AppEnv> {
    override fun get(): AppEnv {
        return appState.env.value
    }

    override infix fun set(new: AppEnv) {
        appState.env.value = new
    }

    override fun compareAndSet(expected: AppEnv, new: AppEnv): Boolean {
        return appState.env.compareAndSet(expected, new)
    }
}
