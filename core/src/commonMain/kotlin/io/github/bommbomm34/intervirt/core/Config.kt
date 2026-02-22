package io.github.bommbomm34.intervirt.core

import com.russhwolf.settings.PreferencesSettings
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.Downloader
import io.github.bommbomm34.intervirt.core.api.Executor
import io.github.bommbomm34.intervirt.core.api.FileManager
import io.github.bommbomm34.intervirt.core.api.QemuClient
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
        (if (get<AppEnv>().PSEUDO_MODE) VirtualGuestManager() else AgentClient(
            get(),
            get(),
        ))
    }.binds(arrayOf(GuestManager::class))
    singleOf(::DeviceManager)
    singleOf(::FileManager)
    singleOf(::QemuClient)
    single {
        AppEnv(
            settings = PreferencesSettings(Preferences.userRoot()),
            override = { System.getenv("INTERVIRT_$it") }
        )
    }
    single {
        HttpClient(CIO) {
            engine {
                requestTimeout = 0
            }
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        }
    }
    single {
        IntervirtConfiguration(
            version = CURRENT_VERSION,
            author = "",
            devices = mutableListOf(
                Device.Switch(
                    id = "switch-88888",
                    name = "My Switch",
                    x = 300,
                    y = 300,
                ),
                Device.Computer(
                    id = "computer-67676",
                    image = "intervirtos/1",
                    name = "My Debian",
                    x = 500,
                    y = 500,
                    ipv4 = "192.168.0.20",
                    ipv6 = "fd00:6767:6767:6767:0808:abcd:abcd:aaaa",
                    mac = "fd:67:67:67:67:67",
                    internetEnabled = false,
                    portForwardings = mutableListOf(
                        PortForwarding("tcp", 67, 25565),
                    ),
                ),
            ),
            connections = mutableListOf(),
        ).apply { connections.add(DeviceConnection.SwitchComputer(devices[0].id, devices[1].id, this)) }
    }
}