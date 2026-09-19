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
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.equals.shouldBeEqual
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

    private val deviceManager: DeviceManager by inject()
    private val guestManager: GuestManager by inject()
    private val _project: ProjectHolder by inject()
    private var project: Project
        get() = _project.get()
        set(value) = _project.set(value)
    private val currentComputer get() = project.getDevice(mockComputer)

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
        project.devices shouldContain computer
    }

    @Test
    fun `should remove device`() = runIntervirtTest {
        deviceManager.addComputer(mockComputer)
        deviceManager.removeDevice(mockComputer).lastResult().bind()
        project.devices shouldNotContain mockComputer
    }

    @Test
    fun `should add switch`() = runIntervirtTest {
        val switch = deviceManager.addSwitch(
            x = 20,
            y = 20,
        )
        project.devices shouldContain switch
    }

    @Test
    fun `should connect computer`() = runIntervirtTest {
        deviceManager.addComputer(mockComputer)
        deviceManager.addComputer(mockComputer2)
        deviceManager.connectDevice(mockComputer, mockComputer2)
        project.connections shouldContain (mockComputer connect mockComputer2)
    }

    @Test
    fun `should disconnect computer`() = runIntervirtTest {
        deviceManager.addComputer(mockComputer)
        deviceManager.addComputer(mockComputer2)
        deviceManager.connectDevice(mockComputer, mockComputer2)
        deviceManager.disconnectDevice(mockComputer, mockComputer2)
        project.connections shouldNotContain (mockComputer connect mockComputer2)
    }

    @Test
    fun `should connect computer to switch`() = runIntervirtTest {
        deviceManager.addComputer(mockComputer)
        val switch = deviceManager.addSwitch(x = 20, y = 20)
        deviceManager.connectDevice(mockComputer, switch)
        project.connections shouldContain (mockComputer connect switch)
    }

    @Test
    fun `should disconnect computer from switch`() = runIntervirtTest {
        deviceManager.addComputer(mockComputer)
        val switch = deviceManager.addSwitch(x = 20, y = 20)
        deviceManager.connectDevice(mockComputer, switch)
        deviceManager.disconnectDevice(mockComputer, switch)
        project.connections shouldNotContain (mockComputer connect switch)
    }

    @Test
    fun `should set IPv4 of device`() = runIntervirtTest {
        deviceManager.addComputer(mockComputer).device
        val ipv4 = randomIpv4(getInfo().ipv4Subnet)
        deviceManager.setIpv4(mockComputer, ipv4)
        currentComputer.ipv4 shouldBeEqual ipv4
    }

    @Test
    fun `should set IPv6 of device`() = runIntervirtTest {
        deviceManager.addComputer(mockComputer).device
        val ipv6 = randomIpv6(getInfo().ipv6Subnet)
        deviceManager.setIpv6(mockComputer, ipv6)
        currentComputer.ipv6 shouldBeEqual ipv6
    }

    @Test
    fun `should set name of device`() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer).device
        deviceManager.setName(computer, "COMPUTER")
        currentComputer.name shouldBeEqual "COMPUTER"
    }

    @Test
    fun `should enable internet of device`() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer).device
        deviceManager.setInternetEnabled(computer, true)
        currentComputer.internetEnabled.shouldBeTrue()
    }

    @Test
    fun `should start computer`() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer).device
        deviceManager.stop(computer) // Computers are running by default
        deviceManager.start(computer)
        currentComputer.running.shouldBeTrue()
    }

    @Test
    fun `should stop computer`() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer).device
        deviceManager.stop(computer)
        currentComputer.running.shouldBeFalse()
    }

    @Test
    fun `should add port forwarding`() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer).device
        deviceManager.addPortForwarding(computer, MOCK_PORT_FORWARDING)
        currentComputer.portForwardings shouldContain MOCK_PORT_FORWARDING
    }

    @Test
    fun `should remove port forwarding`() = runIntervirtTest {
        val computer = deviceManager.addComputer(mockComputer).device
        deviceManager.addPortForwarding(computer, MOCK_PORT_FORWARDING)
        deviceManager.removePortForwarding(MOCK_PORT_FORWARDING.externalPort, MOCK_PORT_FORWARDING.protocol)

        currentComputer.portForwardings shouldNotContain MOCK_PORT_FORWARDING
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

    companion object {
        val MOCK_PORT_FORWARDING = PortForwarding(
            protocol = "tcp",
            externalPort = 2222,
            internalPort = 22,
        )
    }
}
