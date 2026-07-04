package io.github.bommbomm34.intervirt.core.api.impl

import io.github.bommbomm34.intervirt.core.api.AppEnvHolder
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.util.atomic

class AtomicAppEnvHolder(appEnv: AppEnv) : AppEnvHolder {
    private var value by atomic(appEnv)

    override fun get(): AppEnv {
        return value
    }

    override fun set(new: AppEnv) {
        value = new
    }
}
