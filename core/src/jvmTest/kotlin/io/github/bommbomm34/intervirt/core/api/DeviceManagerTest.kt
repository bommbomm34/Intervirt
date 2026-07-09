/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import io.github.bommbomm34.intervirt.core.api.atomic.Holder
import io.github.bommbomm34.intervirt.core.api.atomic.ProjectHolder
import io.github.bommbomm34.intervirt.core.api.impl.AgentGuestManager
import io.github.bommbomm34.intervirt.core.api.impl.DefaultExecutor
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.*
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.core.singleAppEnvHolder
import io.github.bommbomm34.intervirt.core.singleProjectHolder
import io.github.bommbomm34.intervirt.core.util.Atomic
import io.github.bommbomm34.intervirt.core.util.ext.lastResult
import io.github.bommbomm34.intervirt.core.util.randomIpv4
import io.github.bommbomm34.intervirt.core.util.randomIpv6
import io.github.bommbomm34.intervirt.core.util.randomMac
import io.github.bommbomm34.intervirt.core.util.runIntervirtTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import kotlin.test.*

class DeviceManagerTest : KoinTest {
    val testModule = module {
        val appEnv = getTestAppEnv()
        single<DeviceManager>()
        if (appEnv.virtualAgentMode) {
            single<GuestManager> { VirtualGuestManager() }
        } else {
            single<AgentGuestManager>() bind GuestManager::class
        }
        single<QemuClient>()
        single<DefaultExecutor>() bind Executor::class
        single<FileManager>()
        singleAppEnvHolder()
        singleProjectHolder()
        single { getHttpClient() }
    }
    lateinit var mockComputer: Device.Computer
    lateinit var mockComputer2: Device.Computer

    val mockPortForwarding = PortForwarding(
        protocol = "tcp",
        externalPort = 2222,
        internalPort = 22,
    )

    private val deviceManager: DeviceManager by inject()
    private val guestManager: GuestManager by inject()
    private val _project: ProjectHolder by inject()
    private var project: Project
        get() = _project.get()
        set(value) = _project.set(value)

    @BeforeTest
    fun setup() = runIntervirtTest {
        startKoin {
            modules(testModule)
        }
        mockComputer = Device.Computer(
            id = "rand-id",
            image = "debian/13",
            name = "hello",
            x = 10,
            y = 10,
            ipv4 = randomIpv4(),
            ipv6 = randomIpv6(),
            mac = randomMac(),
            internetEnabled = false,
            portForwardings = emptyList(),
        )
        mockComputer2 = Device.Computer(
            id = "rand-id2",
            image = "debian/13",
            name = "hello",
            x = 10,
            y = 10,
            ipv4 = randomIpv4(),
            ipv6 = randomIpv6(),
            mac = randomMac(),
            internetEnabled = false,
            portForwardings = emptyList(),
        )
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
        val ipv4 = randomIpv4(getInfo().ipv4Subnet)
        deviceManager.setIpv4(mockComputer, ipv4)
        assertEquals(project.getDevice(computer).ipv4, ipv4)
    }

    @Test
    fun shouldSetIpv6() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer)
        val ipv6 = randomIpv6(getInfo().ipv6Subnet)
        deviceManager.setIpv6(mockComputer, ipv6)
        assertEquals(project.getDevice(computer).ipv6, ipv6)
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
        deviceManager.stop(computer) // Computers are running by default
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
    fun tearDown() = runIntervirtTest {
        guestManager.wipe().lastResult().bind()
        deviceManager.close()
        stopKoin()
    }

    context(_: Raise<Failure>)
    private suspend fun getInfo(): AgentInfo = guestManager.getInfo()

    context(_: Raise<Failure>)
    private suspend fun randomIpv4(): String = randomIpv4(getInfo().ipv4Subnet)

    context(_: Raise<Failure>)
    private suspend fun randomIpv6(): String = randomIpv6(getInfo().ipv6Subnet)
}
