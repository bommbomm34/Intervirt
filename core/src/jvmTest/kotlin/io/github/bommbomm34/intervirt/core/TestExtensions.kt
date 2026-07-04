package io.github.bommbomm34.intervirt.core

import io.github.bommbomm34.intervirt.core.api.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.impl.AtomicAppEnvHolder
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import org.apache.commons.lang3.compare.ComparableUtils.ge
import org.koin.core.component.KoinComponent
import org.koin.core.module.Module
import org.koin.test.KoinTest
import org.koin.test.get

internal fun Module.singleAppEnvHolder(appEnv: AppEnv = getTestAppEnv()) =
    single<AppEnvHolder> { AtomicAppEnvHolder(appEnv) }

fun KoinTest.injectAppEnv() = lazy { get<AppEnvHolder>().get() }
