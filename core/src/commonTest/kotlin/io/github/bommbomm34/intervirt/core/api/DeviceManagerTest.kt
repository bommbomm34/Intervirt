package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.connect
import io.github.bommbomm34.intervirt.core.getAppEnv
import io.github.bommbomm34.intervirt.core.getHttpClient
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.binds
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.*

class DeviceManagerTest : KoinTest {
    val testModule = module {
        singleOf(::DeviceManager)
        singleOf(::VirtualGuestManager).binds(arrayOf(GuestManager::class))
        singleOf(::QemuClient)
        singleOf(::Executor)
        singleOf(::FileManager)
        single {
            getAppEnv {
                VIRTUAL_AGENT_MODE = true
                VIRTUAL_CONTAINER_IO = true
            }
        }
        single { IntervirtConfiguration() }
        single { getHttpClient() }
    }
    val mockComputer = Device.Computer(
        id = "rand-id",
        image = "my-image",
        name = "hello",
        x = 10,
        y = 10,
        ipv4 = "0.0.0.0",
        ipv6 = "::1",
        mac = "ff:ff:ff:ff:ff:ff",
        internetEnabled = false,
        portForwardings = mutableListOf(),
    )

    val mockComputer2 = Device.Computer(
        id = "rand-id2",
        image = "my-image",
        name = "hello",
        x = 10,
        y = 10,
        ipv4 = "0.1.0.0",
        ipv6 = "::2",
        mac = "ff:ff:2f:ff:ff:ff",
        internetEnabled = false,
        portForwardings = mutableListOf(),
    )

    val mockPortForwarding = PortForwarding(
        protocol = "tcp",
        externalPort = 2222,
        internalPort = 22,
    )

    private val deviceManager: DeviceManager by inject()
    private val configuration: IntervirtConfiguration by inject()

    @BeforeTest
    fun setup() {
        startKoin {
            modules(testModule)
        }
    }

    @Test
    fun testAddComputer() = runTest {
        val computer = deviceManager.addComputer(mockComputer).getOrThrow()
        assertContains(configuration.devices, computer)
    }

    @Test
    fun testRemoveDevice() = runTest {
        deviceManager.addComputer(mockComputer)
        deviceManager.removeDevice(mockComputer).getOrThrow()
        assertFalse { configuration.devices.contains(mockComputer) }
    }

    @Test
    fun testAddSwitch() = runTest {
        val switch = deviceManager.addSwitch(
            x = 20,
            y = 20,
        )
        assertContains(configuration.devices, switch)
    }

    @Test
    fun testConnectComputer() = runTest {
        deviceManager.addComputer(mockComputer).getOrThrow()
        deviceManager.addComputer(mockComputer2).getOrThrow()
        deviceManager.connectDevice(mockComputer, mockComputer2).getOrThrow()
        assertContains(
            iterable = configuration.connections,
            element = configuration.connect(mockComputer, mockComputer2),
        )
    }

    @Test
    fun testDisconnectComputer() = runTest {
        deviceManager.addComputer(mockComputer).getOrThrow()
        deviceManager.addComputer(mockComputer2).getOrThrow()
        deviceManager.connectDevice(mockComputer, mockComputer2).getOrThrow()
        deviceManager.disconnectDevice(mockComputer, mockComputer2).getOrThrow()
        assertFalse {
            configuration.connections.contains(
                configuration.connect(mockComputer, mockComputer2),
            )
        }
    }

    @Test
    fun testConnectComputerSwitch() = runTest {
        deviceManager.addComputer(mockComputer).getOrThrow()
        val switch = deviceManager.addSwitch(x = 20, y = 20)
        deviceManager.connectDevice(mockComputer, switch).getOrThrow()
        assertContains(
            iterable = configuration.connections,
            element = configuration.connect(mockComputer, switch),
        )
    }

    @Test
    fun testDisconnectComputerSwitch() = runTest {
        deviceManager.addComputer(mockComputer).getOrThrow()
        val switch = deviceManager.addSwitch(x = 20, y = 20)
        deviceManager.connectDevice(mockComputer, switch).getOrThrow()
        deviceManager.disconnectDevice(mockComputer, switch).getOrThrow()
        assertFalse {
            configuration.connections.contains(
                configuration.connect(mockComputer, switch),
            )
        }
    }

    @Test
    fun testSetIpv4() = runTest {
        val computer = deviceManager.addComputer(mockComputer).getOrThrow()
        deviceManager.setIpv4(mockComputer, "192.168.0.200").getOrThrow()
        assertEquals(computer.ipv4, "192.168.0.200")
    }

    @Test
    fun testSetIpv6() = runTest {
        val computer = deviceManager.addComputer(mockComputer).getOrThrow()
        deviceManager.setIpv6(mockComputer, "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff").getOrThrow()
        assertEquals(computer.ipv6, "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")
    }

    @Test
    fun testSetName() = runTest {
        val computer = deviceManager.addComputer(mockComputer).getOrThrow()
        deviceManager.setName(computer, "COMPUTER")
        assertEquals(computer.name, "COMPUTER")
    }

    @Test
    fun testSetInternetEnabled() = runTest {
        val computer = deviceManager.addComputer(mockComputer).getOrThrow()
        deviceManager.setInternetEnabled(computer, true).getOrThrow()
        assertEquals(computer.internetEnabled, true)
    }

    @Test
    fun testStartComputer() = runTest {
        val computer = deviceManager.addComputer(mockComputer).getOrThrow()
        deviceManager.start(computer).getOrThrow()
    }

    @Test
    fun testStopComputer() = runTest {
        val computer = deviceManager.addComputer(mockComputer).getOrThrow()
        deviceManager.stop(computer).getOrThrow()
    }

    @Test
    fun testAddPortForwarding() = runTest {
        val computer = deviceManager.addComputer(mockComputer).getOrThrow()
        deviceManager.addPortForwarding(computer, mockPortForwarding).getOrThrow()
        assertContains(computer.portForwardings, mockPortForwarding)
    }

    @Test
    fun testRemovePortForwarding() = runTest {
        val computer = deviceManager.addComputer(mockComputer).getOrThrow()
        deviceManager.addPortForwarding(computer, mockPortForwarding).getOrThrow()
        deviceManager.removePortForwarding(mockPortForwarding.externalPort, mockPortForwarding.protocol).getOrThrow()
        assertFalse { computer.portForwardings.contains(mockPortForwarding) }
    }

    @Test
    fun testGetIOClient() = runTest {
        val computer = deviceManager.addComputer(mockComputer).getOrThrow()
        deviceManager.getIOClient(computer).getOrThrow()
    }

    @Test
    fun testCloseDeviceManager() = runTest {
        deviceManager.close().getOrThrow()
    }

    @AfterTest
    fun tearDown() = stopKoin()
}