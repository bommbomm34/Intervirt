package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.env.AppEnv

interface AppEnvUpdater {
    infix fun set(new: AppEnv)
}
