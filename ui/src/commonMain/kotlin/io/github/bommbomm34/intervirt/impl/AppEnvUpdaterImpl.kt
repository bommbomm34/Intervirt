package io.github.bommbomm34.intervirt.impl

import io.github.bommbomm34.intervirt.core.api.AppEnvUpdater
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.data.AppState

class AppEnvUpdaterImpl(private val appState: AppState) : AppEnvUpdater {
    override infix fun set(new: AppEnv) {
        appState.env = new
    }
}
