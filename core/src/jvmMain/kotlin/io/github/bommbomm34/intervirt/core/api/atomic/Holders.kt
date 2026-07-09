package io.github.bommbomm34.intervirt.core.api.atomic

import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.data.env.AppEnv

class AppEnvHolder(private val holder: Holder<AppEnv>) : Holder<AppEnv> by holder

class ProjectHolder(private val holder: Holder<Project>) : Holder<Project> by holder
