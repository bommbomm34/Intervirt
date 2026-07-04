package io.github.bommbomm34.intervirt.impl

import io.github.bommbomm34.intervirt.core.api.AppEnvHolder
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.data.AppState

class AppEnvHolderImpl(private val appState: AppState) : AppEnvHolder {
    override fun get(): AppEnv {
        return appState.env
    }

    override infix fun set(new: AppEnv) {
        appState.env = new
    }
}
