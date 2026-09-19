/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import io.github.bommbomm34.intervirt.core.api.*
import io.github.bommbomm34.intervirt.core.api.atomic.ProjectHolder
import io.github.bommbomm34.intervirt.core.api.atomic.modify
import io.github.bommbomm34.intervirt.core.api.impl.DefaultExecutor
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.*
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.core.singleTestSettings
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.model.DeviceSettingsViewModel
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldEqual
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
    fun `should open shell`() {
        viewModel.openShell()
        testComputer shouldEqual appState.openComputerShell
    }

    @Test
    fun `should toggle port forwardings`() {
        viewModel.togglePortForwardings()
        viewModel.showPortForwardings.shouldBeTrue()
        viewModel.togglePortForwardings()
        viewModel.showPortForwardings.shouldBeFalse()
    }

    @Test
    fun `should start device`() = runTest {
        viewModel.start().join()
        testComputer.running.shouldBeTrue()
    }

    @Test
    fun `should stop device`() = runTest {
        viewModel.start().join()
        viewModel.stop().join()
        testComputer.running.shouldBeFalse()
    }

    @Test
    fun `should change IPv4 of device`() = runTest {
        viewModel.changeIpv4("0.0.0.1").join()
        testComputer.ipv4 shouldBeEqual "0.0.0.1"
    }

    @Test
    fun `should change IPv6 of device`() = runTest {
        viewModel.changeIpv6("::1").join()
        testComputer.ipv6 shouldBeEqual "::1"
    }

    @Test
    fun `should enable internet access`() = runTest {
        viewModel.enableInternetAccess(true).join()
        testComputer.internetEnabled.shouldBeTrue()
    }

    @Test
    fun `should add port forwarding`() = runTest {
        viewModel.addPortForwarding(TEST_PORT_FORWARDING).join()
        testComputer.portForwardings shouldContain TEST_PORT_FORWARDING
    }

    @Test
    fun `should remove port forwarding`() = runTest {
        viewModel.addPortForwarding(TEST_PORT_FORWARDING).join()
        viewModel.removePortForwarding(TEST_PORT_FORWARDING).join()
        testComputer.portForwardings shouldNotContain TEST_PORT_FORWARDING
    }

    @Test
    fun `should lint port forwarding which is already internally exposed`() = runTest {
        if (!isRunningOnCi()) {
            viewModel.addPortForwarding(TEST_PORT_FORWARDING).join()
            viewModel.lintPortForwarding(TEST_PORT_FORWARDING).shouldBeLeft()
        }
    }

    @Test
    fun `should lint port forwarding which is already externally exposed`() = runTest {
        if (!isRunningOnCi()) {
            val secondTestComputer = Device.Computer.portForwardings.modify(testComputer) {
                it + TEST_PORT_FORWARDING
            }
            Project.devices.modify(project) { it + secondTestComputer }
            viewModel.lintPortForwarding(TEST_PORT_FORWARDING).shouldBeLeft()
        }
    }

    @Test
    fun `should lint port forwarding which is already bound`() = runTest {
        ServerSocket(0).use {
            val fwd = TEST_PORT_FORWARDING.copy(externalPort = it.localPort)
            viewModel.lintPortForwarding(fwd).shouldBeLeft()
        }
    }

    @Test
    fun `should lint port forwarding successfully`() = runTest {
        viewModel.lintPortForwarding(TEST_PORT_FORWARDING).shouldBeRight()
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

