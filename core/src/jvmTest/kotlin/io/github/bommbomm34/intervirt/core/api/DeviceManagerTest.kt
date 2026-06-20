/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.api.impl.DefaultExecutor
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.*
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.core.singleProject
import io.github.bommbomm34.intervirt.core.util.Atomic
import io.github.bommbomm34.intervirt.core.util.runIntervirtTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.*

class DeviceManagerTest : KoinTest {
    val testModule = module {
        single<DeviceManager>()
        single<GuestManager> { VirtualGuestManager() }
        single<QemuClient>()
        single<DefaultExecutor>() bind Executor::class
        single<FileManager>()
        single { getTestAppEnv() }
        singleProject()
        single { getHttpClient() }
    }
    val mockComputer = Device.Computer(
        id = "rand-id",
        image = "debian/13",
        name = "hello",
        x = 10,
        y = 10,
        ipv4 = "0.0.0.0",
        ipv6 = "::1",
        mac = "ff:ff:ff:ff:ff:ff",
        internetEnabled = false,
        portForwardings = listOf(),
    )

    val mockComputer2 = Device.Computer(
        id = "rand-id2",
        image = "debian/13",
        name = "hello",
        x = 10,
        y = 10,
        ipv4 = "0.1.0.0",
        ipv6 = "::2",
        mac = "ff:ff:2f:ff:ff:ff",
        internetEnabled = false,
        portForwardings = listOf(),
    )

    val mockPortForwarding = PortForwarding(
        protocol = "tcp",
        externalPort = 2222,
        internalPort = 22,
    )

    private val deviceManager: DeviceManager by inject()
    private val _project: Atomic<Project> by inject()
    private var project: Project
        get() = _project.get()
        set(value) = _project.set(value)

    @BeforeTest
    fun setup() {
        startKoin {
            modules(testModule)
        }
    }

    @Test
    fun shouldAddComputer() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer)
        assertContains(project.devices, computer)
    }

    @Test
    fun shouldRemoveDevice() = runIntervirtTest {
        deviceManager.addComputer(mockComputer)
        deviceManager.removeDevice(mockComputer)
        assertFalse { project.devices.contains(mockComputer) }
    }

    @Test
    fun shouldAddSwitch() = runIntervirtTest {
        val switch = deviceManager.addSwitch(
            x = 20,
            y = 20,
        )
        assertContains(project.devices, switch)
    }

    @Test
    fun shouldConnectComputer() = runIntervirtTest {
        deviceManager.addComputer(mockComputer)
        deviceManager.addComputer(mockComputer2)
        deviceManager.connectDevice(mockComputer, mockComputer2)
        assertContains(
            iterable = project.connections,
            element = mockComputer connect mockComputer2,
        )
    }

    @Test
    fun shouldDisconnectComputer() = runIntervirtTest {
        deviceManager.addComputer(mockComputer)
        deviceManager.addComputer(mockComputer2)
        deviceManager.connectDevice(mockComputer, mockComputer2)
        deviceManager.disconnectDevice(mockComputer, mockComputer2)
        assertFalse {
            project.connections.contains(mockComputer connect mockComputer2)
        }
    }

    @Test
    fun shouldConnectComputerSwitch() = runIntervirtTest {
        deviceManager.addComputer(mockComputer)
        val switch = deviceManager.addSwitch(x = 20, y = 20)
        deviceManager.connectDevice(mockComputer, switch)
        assertContains(
            iterable = project.connections,
            element = mockComputer connect switch,
        )
    }

    @Test
    fun shouldDisconnectComputerSwitch() = runIntervirtTest {
        deviceManager.addComputer(mockComputer)
        val switch = deviceManager.addSwitch(x = 20, y = 20)
        deviceManager.connectDevice(mockComputer, switch)
        deviceManager.disconnectDevice(mockComputer, switch)
        assertFalse {
            project.connections.contains(mockComputer connect switch)
        }
    }

    @Test
    fun shouldSetIpv4() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer)
        deviceManager.setIpv4(mockComputer, "192.168.0.200")
        assertEquals(project.getDevice(computer).ipv4, "192.168.0.200")
    }

    @Test
    fun shouldSetIpv6() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer)
        deviceManager.setIpv6(mockComputer, "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")
        assertEquals(project.getDevice(computer).ipv6, "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")
    }

    @Test
    fun shouldSetName() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer)
        deviceManager.setName(computer, "COMPUTER")
        assertEquals(project.getDevice(computer).name, "COMPUTER")
    }

    @Test
    fun shouldSetInternetEnabled() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer)
        deviceManager.setInternetEnabled(computer, true)
        assertEquals(project.getDevice(computer).internetEnabled, true)
    }

    @Test
    fun shouldStartComputer() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer)
        deviceManager.start(computer)
    }

    @Test
    fun shouldStopComputer() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer)
        deviceManager.stop(computer)
    }

    @Test
    fun shouldAddPortForwarding() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer)
        deviceManager.addPortForwarding(computer, mockPortForwarding)
        assertContains(project.getDevice(computer).portForwardings, mockPortForwarding)
    }

    @Test
    fun shouldRemovePortForwarding() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer)
        deviceManager.addPortForwarding(computer, mockPortForwarding)
        deviceManager.removePortForwarding(mockPortForwarding.externalPort, mockPortForwarding.protocol)
        assertFalse { computer.portForwardings.contains(mockPortForwarding) }
    }

    @Test
    fun shouldGetIOClient() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer)
        deviceManager.getIOClient(computer)
    }

    @Test
    fun shouldCloseDeviceManager() = runIntervirtTest {
        deviceManager.close()
    }

    @AfterTest
    fun tearDown() = stopKoin()
}
