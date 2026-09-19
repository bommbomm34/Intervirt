/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import io.github.bommbomm34.intervirt.core.api.atomic.ProjectHolder
import io.github.bommbomm34.intervirt.core.api.impl.AgentGuestManager
import io.github.bommbomm34.intervirt.core.api.impl.DefaultExecutor
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.*
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.core.singleAppEnvHolder
import io.github.bommbomm34.intervirt.core.singleProjectHolder
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
            id = DeviceId("computer-10001"),
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
            id = DeviceId("computer-10002"),
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
    fun `should add computer`() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer).device
        assertContains(project.devices, computer)
    }

    @Test
    fun `should remove device`() = runIntervirtTest {
        deviceManager.addComputer(mockComputer)
        deviceManager.removeDevice(mockComputer).lastResult().bind()
        assertFalse { project.devices.contains(mockComputer) }
    }

    @Test
    fun `should add switch`() = runIntervirtTest {
        val switch = deviceManager.addSwitch(
            x = 20,
            y = 20,
        )
        assertContains(project.devices, switch)
    }

    @Test
    fun `should connect computer`() = runIntervirtTest {
        deviceManager.addComputer(mockComputer)
        deviceManager.addComputer(mockComputer2)
        deviceManager.connectDevice(mockComputer, mockComputer2)
        assertContains(
            iterable = project.connections,
            element = mockComputer connect mockComputer2,
        )
    }

    @Test
    fun `should disconnect computer`() = runIntervirtTest {
        deviceManager.addComputer(mockComputer)
        deviceManager.addComputer(mockComputer2)
        deviceManager.connectDevice(mockComputer, mockComputer2)
        deviceManager.disconnectDevice(mockComputer, mockComputer2)
        assertFalse {
            project.connections.contains(mockComputer connect mockComputer2)
        }
    }

    @Test
    fun `should connect computer to switch`() = runIntervirtTest {
        deviceManager.addComputer(mockComputer)
        val switch = deviceManager.addSwitch(x = 20, y = 20)
        deviceManager.connectDevice(mockComputer, switch)
        assertContains(
            iterable = project.connections,
            element = mockComputer connect switch,
        )
    }

    @Test
    fun `should disconnect computer from switch`() = runIntervirtTest {
        deviceManager.addComputer(mockComputer)
        val switch = deviceManager.addSwitch(x = 20, y = 20)
        deviceManager.connectDevice(mockComputer, switch)
        deviceManager.disconnectDevice(mockComputer, switch)
        assertFalse {
            project.connections.contains(mockComputer connect switch)
        }
    }

    @Test
    fun `should set IPv4 of device`() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer).device
        val ipv4 = randomIpv4(getInfo().ipv4Subnet)
        deviceManager.setIpv4(mockComputer, ipv4)
        assertEquals(project.getDevice(computer).ipv4, ipv4)
    }

    @Test
    fun `should set IPv6 of device`() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer).device
        val ipv6 = randomIpv6(getInfo().ipv6Subnet)
        deviceManager.setIpv6(mockComputer, ipv6)
        assertEquals(project.getDevice(computer).ipv6, ipv6)
    }

    @Test
    fun `should set name of device`() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer).device
        deviceManager.setName(computer, "COMPUTER")
        assertEquals("COMPUTER", project.getDevice(computer).name)
    }

    @Test
    fun `should enable internet of device`() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer).device
        deviceManager.setInternetEnabled(computer, true)
        assertEquals(true, project.getDevice(computer).internetEnabled)
    }

    @Test
    fun `should start computer`() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer).device
        deviceManager.stop(computer) // Computers are running by default
        deviceManager.start(computer)
    }

    @Test
    fun `should stop computer`() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer).device
        deviceManager.stop(computer)
    }

    @Test
    fun `should add port forwarding`() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer).device
        deviceManager.addPortForwarding(computer, mockPortForwarding)
        assertContains(project.getDevice(computer).portForwardings, mockPortForwarding)
    }

    @Test
    fun `should remove port forwarding`() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer).device
        deviceManager.addPortForwarding(computer, mockPortForwarding)
        deviceManager.removePortForwarding(mockPortForwarding.externalPort, mockPortForwarding.protocol)
        assertFalse { computer.portForwardings.contains(mockPortForwarding) }
    }

    @Test
    fun `should get IO client of device`() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer).device
        deviceManager.getIOClient(computer)
    }

    @Test
    fun `should close DeviceManager`() = runIntervirtTest {
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
