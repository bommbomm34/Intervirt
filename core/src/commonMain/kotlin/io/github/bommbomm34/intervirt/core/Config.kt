package io.github.bommbomm34.intervirt.core

import com.russhwolf.settings.PreferencesSettings
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.Downloader
import io.github.bommbomm34.intervirt.core.api.Executor
import io.github.bommbomm34.intervirt.core.api.FileManager
import io.github.bommbomm34.intervirt.core.api.QemuClient
import io.github.bommbomm34.intervirt.core.api.*
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
        (if (get<io.github.bommbomm34.intervirt.core.data.AppEnv>().PSEUDO_MODE) _root_ide_package_.io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager() else _root_ide_package_.io.github.bommbomm34.intervirt.core.api.impl.AgentClient(
            get(),
            get(),
        ))
    }.binds(arrayOf(_root_ide_package_.io.github.bommbomm34.intervirt.core.api.GuestManager::class))
    singleOf(::DeviceManager)
    singleOf(::FileManager)
    singleOf(::QemuClient)
    single { _root_ide_package_.io.github.bommbomm34.intervirt.core.data.AppEnv(PreferencesSettings(Preferences.userRoot())) }
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
        _root_ide_package_.io.github.bommbomm34.intervirt.core.data.IntervirtConfiguration(
            version = _root_ide_package_.io.github.bommbomm34.intervirt.core.CURRENT_VERSION,
            author = "",
            devices = mutableListOf(
                _root_ide_package_.io.github.bommbomm34.intervirt.core.data.Device.Switch(
                    id = "switch-88888",
                    name = "My Switch",
                    x = 300,
                    y = 300,
                ),
                _root_ide_package_.io.github.bommbomm34.intervirt.core.data.Device.Computer(
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
                        _root_ide_package_.io.github.bommbomm34.intervirt.core.data.PortForwarding("tcp", 67, 25565),
                    ),
                ),
            ),
            connections = mutableListOf(),
        ).apply { connections.add(_root_ide_package_.io.github.bommbomm34.intervirt.core.data.DeviceConnection.SwitchComputer(devices[0].id, devices[1].id, this)) }
    }
}