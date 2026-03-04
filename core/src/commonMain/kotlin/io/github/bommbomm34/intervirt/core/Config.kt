package io.github.bommbomm34.intervirt.core

import io.github.bommbomm34.intervirt.core.api.*
import io.github.bommbomm34.intervirt.core.api.impl.AgentClient
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.secret.SecretService
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.binds
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single


const val CURRENT_VERSION = "0.0.1"

val defaultJson = Json {
    ignoreUnknownKeys = true
}

val coreModule = module {
    singleOf(::Executor)
    singleOf(::Downloader)
    single {
        (if (get<AppEnv>().VIRTUAL_AGENT_MODE) VirtualGuestManager() else AgentClient(
            get(),
            get(),
        ))
    }.binds(arrayOf(GuestManager::class))
    singleOf(::DeviceManager)
    singleOf(::FileManager)
    singleOf(::QemuClient)
    single { SecretService("io.github.bommbomm34.intervirt") }
    single { getAppEnv() }
    single { getHttpClient() }
    single { IntervirtConfiguration.default() }
}