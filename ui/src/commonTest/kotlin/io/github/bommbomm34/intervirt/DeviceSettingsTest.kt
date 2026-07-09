/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import io.github.bommbomm34.intervirt.core.api.*
import io.github.bommbomm34.intervirt.core.api.atomic.Holder
import io.github.bommbomm34.intervirt.core.api.atomic.ProjectHolder
import io.github.bommbomm34.intervirt.core.api.impl.DefaultExecutor
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.*
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.core.singleTestSettings
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.model.DeviceSettingsViewModel
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel
import org.koin.test.KoinTest
import org.koin.test.inject
import java.net.ServerSocket
import kotlin.test.*

class DeviceSettingsTest : KoinTest {
    val viewModel: DeviceSettingsViewModel by inject { parametersOf(TEST_COMPUTER.id) }
    val appState: AppState by inject()
    val deviceManager: DeviceManager by inject()
    val project: ProjectHolder by inject()
    val testComputer: Device.Computer get() = project.get().getDevice(TEST_COMPUTER)

    @BeforeTest
    fun init() = runIntervirtTest {
        startKoin {
            modules(
                module {
                    single { getTestAppEnv() }
                    single { getHttpClient() }
                    single<GuestManager> { VirtualGuestManager() }
                    singleTestAppState()
                    singleProjectHolder()
                    singleTestSettings()
                    singleAppEnvHolder()
                    single<FileManager>()
                    single<QemuClient>()
                    single<DefaultExecutor>() bind Executor::class
                    single<DeviceManager>()
                    viewModel<DeviceSettingsViewModel>()
                },
            )
        }
        deviceManager.addComputer(TEST_COMPUTER)
    }

    @Test
    fun shouldOpenShell() {
        viewModel.openShell()
        assertEquals(testComputer, appState.openComputerShell)
    }

    @Test
    fun shouldTogglePortForwardings() {
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
        println(testComputer)
        viewModel.start().join()
        println(testComputer)
        viewModel.stop().join()
        println(testComputer)
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
        viewModel.addPortForwarding(TEST_PORT_FORWARDING).join()
        assertContains(testComputer.portForwardings, TEST_PORT_FORWARDING)
    }

    @Test
    fun shouldRemovePortForwarding() = runTest {
        viewModel.addPortForwarding(TEST_PORT_FORWARDING).join()
        viewModel.removePortForwarding(TEST_PORT_FORWARDING).join()
        assertFalse { testComputer.portForwardings.contains(TEST_PORT_FORWARDING) }
    }

    @Ignore
    @Test
    fun shouldLintPortForwardingThatIsAlreadyInternallyExposed() = runTest {
        viewModel.addPortForwarding(TEST_PORT_FORWARDING).join()
        assertEquals(false, viewModel.lintPortForwarding(TEST_PORT_FORWARDING).isRight())
    }

    @Ignore
    @Test
    fun shouldLintPortForwardingThatIsAlreadyExternallyExposed() = runTest {
        val secondTestComputer = Device.Computer.portForwardings.modify(testComputer) {
            it + TEST_PORT_FORWARDING
        }
        Project.devices.modify(project.get()) { it + secondTestComputer }
        assertEquals(false, viewModel.lintPortForwarding(TEST_PORT_FORWARDING).isRight())
    }

    @Test
    fun shouldLintPortForwardingThatIsAlreadyBound() = runTest {
        ServerSocket(0).use {
            val fwd = TEST_PORT_FORWARDING.copy(externalPort = it.localPort)
            assertEquals(false, viewModel.lintPortForwarding(fwd).isRight())
        }
    }

    @Test
    fun shouldLintPortForwardingSuccessfully() = runTest {
        assertEquals(true, viewModel.lintPortForwarding(TEST_PORT_FORWARDING).isRight())
    }

    @AfterTest
    fun stop() {
        stopKoin()
    }

    companion object {
        val TEST_COMPUTER = Device.Computer(
            id = DeviceId("computer-22222"),
            image = "debian/13",
            name = "None",
            x = 0,
            y = 0,
            ipv4 = "0.0.0.0",
            ipv6 = "::",
            mac = "ff:ff:ff:ff:ff:ff",
            internetEnabled = false,
            portForwardings = emptyList(),
        )

        val TEST_PORT_FORWARDING = PortForwarding(
            protocol = "tcp",
            externalPort = 2222,
            internalPort = 22,
        )
    }
}

