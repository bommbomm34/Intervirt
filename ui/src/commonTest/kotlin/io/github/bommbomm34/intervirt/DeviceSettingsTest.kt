/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.Executor
import io.github.bommbomm34.intervirt.core.api.FileManager
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.api.QemuClient
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.getAppEnv
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewDevice
import io.github.bommbomm34.intervirt.data.toViewDevice
import io.github.bommbomm34.intervirt.model.DeviceSettingsViewModel
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel
import org.koin.test.KoinTest
import org.koin.test.inject
import java.net.ServerSocket
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class DeviceSettingsTest : KoinTest {
    val testComputer = Device.Computer(
        id = "computer-22222",
        image = "debian/13",
        name = "None",
        x = 0,
        y = 0,
        ipv4 = "0.0.0.0",
        ipv6 = "::",
        mac = "ff:ff:ff:ff:ff:ff",
        internetEnabled = false,
        portForwardings = mutableListOf(),
    ).toViewDevice() as ViewDevice.Computer
    val testPortForwarding = PortForwarding(
        protocol = "tcp",
        externalPort = 2222,
        internalPort = 22,
    )

    val viewModel: DeviceSettingsViewModel by inject { parametersOf(testComputer) }
    val appState: AppState by inject()
    val deviceManager: DeviceManager by inject()
    val configuration: IntervirtConfiguration by inject()

    @BeforeTest
    fun init() = runTest {
        startKoin {
            modules(
                module {
                    single<AppState>()
                    single { getTestAppEnv() }
                    single { getHttpClient() }
                    single<GuestManager> { VirtualGuestManager() }
                    single<IntervirtConfiguration> { IntervirtConfiguration.default() }
                    single<FileManager>()
                    single<QemuClient>()
                    single<Executor>()
                    single<DeviceManager>()
                    viewModel<DeviceSettingsViewModel>()
                },
            )
        }
        deviceManager.addComputer(testComputer.device).getOrThrow()
    }

    @Test
    fun shouldOpenShell(){
        viewModel.openShell()
        assertEquals(testComputer, appState.openComputerShell)
    }

    @Test
    fun shouldTogglePortForwardings(){
        viewModel.togglePortForwardings()
        assertEquals(true, viewModel.showPortForwardings)
        viewModel.togglePortForwardings()
        assertEquals(false, viewModel.showPortForwardings)
    }

    @Test
    fun shouldStart() = runTest {
        viewModel.start().join()
        assertEquals(true, testComputer.running)
    }

    @Test
    fun shouldStop() = runTest {
        viewModel.start().join()
        viewModel.stop().join()
        assertEquals(false, testComputer.running)
    }

    @Test
    fun shouldChangeIpv4() = runTest {
        viewModel.changeIpv4("0.0.0.1").join()
        assertEquals("0.0.0.1", testComputer.ipv4)
    }

    @Test
    fun shouldChangeIpv6() = runTest {
        viewModel.changeIpv6("::1").join()
        assertEquals("::1", testComputer.ipv6)
    }

    @Test
    fun shouldEnableInternetAccess() = runTest {
        viewModel.enableInternetAccess(true).join()
        assertEquals(true, testComputer.internetEnabled)
    }

    @Test
    fun shouldAddPortForwarding() = runTest {
        viewModel.addPortForwarding(testPortForwarding).join()
        assertContains(testComputer.portForwardings, testPortForwarding)
    }

    @Test
    fun shouldRemovePortForwarding() = runTest {
        viewModel.addPortForwarding(testPortForwarding).join()
        viewModel.removePortForwarding(testPortForwarding).join()
        assertFalse { testComputer.portForwardings.contains(testPortForwarding) }
    }

    @Ignore
    @Test
    fun shouldLintPortForwardingThatIsAlreadyInternallyExposed() = runTest {
        viewModel.addPortForwarding(testPortForwarding).join()
        assertEquals(false, viewModel.lintPortForwarding(testPortForwarding).isSuccess)
    }

    @Ignore
    @Test
    fun shouldLintPortForwardingThatIsAlreadyExternallyExposed() = runTest {
        val secondTestComputer = testComputer.device.copy().apply {
            portForwardings.add(PortForwarding("tcp", 2222, 22))
        }
        configuration.devices.add(secondTestComputer)
        assertEquals(false, viewModel.lintPortForwarding(testPortForwarding).isSuccess)
    }

    @Test
    fun shouldLintPortForwardingThatIsAlreadyBound() = runTest {
        ServerSocket(0).use {
            val fwd = testPortForwarding.copy(externalPort = it.localPort)
            assertEquals(false, viewModel.lintPortForwarding(fwd).isSuccess)
        }
    }

    @Test
    fun shouldLintPortForwardingSuccessfully() = runTest {
        assertEquals(true, viewModel.lintPortForwarding(testPortForwarding).isSuccess)
    }

    @AfterTest
    fun stop() {
        stopKoin()
    }
}

