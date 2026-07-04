/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import io.github.bommbomm34.intervirt.core.api.*
import io.github.bommbomm34.intervirt.core.api.impl.AgentGuestManager
import io.github.bommbomm34.intervirt.core.api.impl.DefaultExecutor
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.toAtomic
import io.github.bommbomm34.intervirt.secret.SecretService
import kotlinx.serialization.json.Json
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import java.util.prefs.Preferences


const val CURRENT_VERSION = "0.0.1"

val defaultJson = Json {
    ignoreUnknownKeys = true
}

val coreModule = module {
    single<DefaultExecutor>() bind Executor::class
    single<Downloader>()
    single<GuestManager> {
        if (get<AppEnvHolder>().get().virtualAgentMode) {
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
            logger = get<AppEnvHolder>().get().getLogger(SecretService::class)
        )
    }
    single { getHttpClient() }
    singleSettings()
    singleProject()
}
