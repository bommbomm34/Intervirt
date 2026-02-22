package io.github.bommbomm34.intervirt.core

import com.russhwolf.settings.PreferencesSettings
import io.github.bommbomm34.intervirt.core.api.*
import io.github.bommbomm34.intervirt.core.api.impl.AgentClient
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.binds
import org.koin.dsl.module
import java.util.prefs.Preferences


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
    single { getAppEnv() }
    single { getHttpClient() }
    single {
        IntervirtConfiguration(
            version = CURRENT_VERSION,
            author = "",
            devices = mutableListOf(),
            connections = mutableListOf(),
        )
    }
}