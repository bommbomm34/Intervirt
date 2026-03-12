/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core

import io.github.bommbomm34.intervirt.core.api.*
import io.github.bommbomm34.intervirt.core.api.impl.AgentGuestManager
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.secret.SecretService
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single


const val CURRENT_VERSION = "0.0.1"

val defaultJson = Json {
    ignoreUnknownKeys = true
}

val coreModule = module {
    single<Executor>()
    single<Downloader>()
    single<GuestManager> {
        if (get<AppEnv>().VIRTUAL_AGENT_MODE) {
            VirtualGuestManager()
        } else {
            AgentGuestManager(get(), get())
        }
    }
    single<DeviceManager>()
    single<FileManager>()
    single<QemuClient>()
    single<ShutdownHandler>()
    single {
        SecretService(
            serviceName = "io.github.bommbomm34.intervirt",
            logger = get<AppEnv>().getLogger(SecretService::class)
        )
    }
    single { getAppEnv() }
    single { getHttpClient() }
    single { Project.default() }
}