package io.github.bommbomm34.intervirt.core

import io.github.bommbomm34.intervirt.core.api.atomic.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.atomic.Holder
import io.github.bommbomm34.intervirt.core.api.atomic.ProjectHolder
import io.github.bommbomm34.intervirt.core.api.atomic.impl.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.atomic.impl.Holder
import io.github.bommbomm34.intervirt.core.api.atomic.impl.ProjectHolder
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import org.koin.core.module.Module
import org.koin.test.KoinTest
import org.koin.test.get

internal fun Module.singleAppEnvHolder(appEnv: AppEnv = getTestAppEnv()) =
    single { AppEnvHolder(appEnv) }

internal fun Module.singleProjectHolder() =
    single { ProjectHolder(Project()) }

fun KoinTest.injectAppEnv() = lazy { get<AppEnvHolder>().get() }
