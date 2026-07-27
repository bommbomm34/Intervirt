/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import io.github.bommbomm34.intervirt.core.CURRENT_VERSION
import io.github.bommbomm34.intervirt.core.api.impl.AgentGuestManager
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.*
import io.github.bommbomm34.intervirt.core.data.agent.ContainerInfo
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.core.singleAppEnvHolder
import io.github.bommbomm34.intervirt.core.util.ext.lastResult
import io.github.bommbomm34.intervirt.core.util.randomIpv4
import io.github.bommbomm34.intervirt.core.util.randomIpv6
import io.github.bommbomm34.intervirt.core.util.randomMac
import io.github.bommbomm34.intervirt.core.util.runIntervirtTest
import io.ktor.client.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.toList
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

private val TEST_CONTAINER_ID = DeviceId("computer-10001")
private const val TEST_NETWORK_NAME = "test-network"

class GuestManagerTest : KoinTest {
    val fwd = PortForwarding(
        protocol = "tcp",
        internalPort = 22,
        externalPort = 2222,
    )
    val guestManager: GuestManager by inject()
    var isVirtual: Boolean = true

    @BeforeTest
    fun startTest() = runIntervirtTest {
        val appEnv = getTestAppEnv()
        startKoin {
            modules(
                module {
                    if (appEnv.virtualAgentMode) {
                        single<GuestManager> { VirtualGuestManager(0.seconds) }
                    } else {
                        isVirtual = false
                        singleAppEnvHolder()
                        single<HttpClient> { getHttpClient() }
                        single<AgentGuestManager>() bind GuestManager::class
                    }
                },
            )
        }
    }

    @Test
    fun shouldAddContainer() = runIntervirtTest {
        val container = addTestContainer()
        assertContains(getContainers(), container)
    }

    @Test
    fun shouldRemoveContainer() = runIntervirtTest {
        val container = addTestContainer()
        guestManager.removeContainer(TEST_CONTAINER_ID).lastResult().bind()
        assertFalse { getContainers().contains(container) }
    }

    @Test
    fun shouldSetIpv4() = runIntervirtTest {
        val container = addTestContainer()
        val newIP = randomIpv4(getInfo().ipv4Subnet)
        guestManager.setIpv4(
            id = TEST_CONTAINER_ID,
            newIP = newIP,
        )
        assertEquals(newIP, container.getContainer().ipv4)
    }

    @Test
    fun shouldSetIpv6() = runIntervirtTest {
        val container = addTestContainer()
        val newIP = randomIpv6(getInfo().ipv6Subnet)
        guestManager.setIpv6(
            id = TEST_CONTAINER_ID,
            newIP = newIP,
        )
        assertEquals(newIP, container.getContainer().ipv6)
    }

    @Test
    fun shouldConnect() = runIntervirtTest {
        addTestContainer()
        addTestNetwork()
        guestManager.connect(TEST_CONTAINER_ID, TEST_NETWORK_NAME)
        assertContains(getNetworks()[TEST_NETWORK_NAME]!!, TEST_CONTAINER_ID)
    }

    @Test
    fun shouldDisconnect() = runIntervirtTest {
        addTestContainer()
        addTestNetwork()
        guestManager.connect(TEST_CONTAINER_ID, TEST_NETWORK_NAME)
        guestManager.disconnect(TEST_CONTAINER_ID, TEST_NETWORK_NAME)
        assertFalse { getNetworks()[TEST_NETWORK_NAME]!!.contains(TEST_CONTAINER_ID) }
    }

    @Test
    fun shouldSetInternetAccess() = runIntervirtTest {
        val container = addTestContainer()
        guestManager.setInternetAccess(TEST_CONTAINER_ID, true)
        assertTrue { container.getContainer().internet }
    }

    @Test
    fun shouldAddPortForwarding() = runIntervirtTest {
        val container = addTestContainer()
        addTestPortForwarding()
        assertContains(container.getContainer().portForwardings, fwd)
    }

    @Test
    fun shouldRemovePortForwarding() = runIntervirtTest {
        val container = addTestContainer()
        addTestPortForwarding()
        guestManager.removePortForwarding(TEST_CONTAINER_ID, fwd.externalPort, fwd.protocol)
        assertFalse { container.getContainer().portForwardings.contains(fwd) }
    }

    @Test
    fun shouldStartContainer() = runIntervirtTest {
        val container = addTestContainer()
        guestManager.stopContainer(TEST_CONTAINER_ID) // Containers start by default
        guestManager.startContainer(TEST_CONTAINER_ID)
        assertTrue { container.getContainer().running }
    }

    @Test
    fun shouldStopContainer() = runIntervirtTest {
        val container = addTestContainer()
        guestManager.stopContainer(TEST_CONTAINER_ID)
        assertFalse { container.getContainer().running }
    }

    @Test
    fun shouldWipe() = runIntervirtTest {
        val container = addTestContainer()
        addTestNetwork()
        val progress = guestManager.wipe().toList()
        assertContains(progress, ResultProgress.success(Unit))
        assertFalse { getContainers().contains(container) }
        assertFalse { getNetworks().containsKey(TEST_NETWORK_NAME) }
    }

    @Test
    fun shouldUpdate() = runIntervirtTest {
        val progress = guestManager.update().toList()
        assertContains(progress, ResultProgress.success(Unit))
    }

    @Test
    fun shouldGetInfo() = runIntervirtTest {
        val info = getInfo()
        if (guestManager is VirtualGuestManager) assertEquals(CURRENT_VERSION, info.version)
    }

    @Test
    fun shouldGetContainers() = runIntervirtTest {
        val container1 = addTestContainer(DeviceId("computer-10002"))
        val container2 = addTestContainer(DeviceId("computer-10003"))
        val container3 = addTestContainer(DeviceId("computer-10005"))
        val containers = getContainers()
        assertContains(containers, container1)
        assertContains(containers, container2)
        assertContains(containers, container3)
    }

    @Test
    fun shouldAddNetwork() = runIntervirtTest {
        addTestNetwork()
        assertContains(getNetworks(), TEST_NETWORK_NAME)
    }

    @Test
    fun shouldRemoveNetwork() = runIntervirtTest {
        addTestNetwork()
        guestManager.removeNetwork(TEST_NETWORK_NAME)
        assertFalse { getNetworks().contains(TEST_NETWORK_NAME) }
    }

    @Test
    fun shouldGetNetworks() = runIntervirtTest {
        addTestNetwork("test-network1")
        addTestNetwork("test-network2")
        addTestNetwork("test-network3")
        val networks = getNetworks()
        assertContains(networks, "test-network1")
        assertContains(networks, "test-network2")
        assertContains(networks, "test-network3")
    }

    @Test
    fun shouldClose() = runIntervirtTest {
        guestManager.close()
    }

    context(_: Raise<Failure>)
    private suspend fun addTestContainer(id: DeviceId = TEST_CONTAINER_ID): ContainerInfo {
        val info = ContainerInfo(
            id = id,
            ipv4 = randomIpv4(getInfo().ipv4Subnet),
            ipv6 = randomIpv6(getInfo().ipv6Subnet),
            mac = randomMac(),
            internet = false,
            image = "debian/13",
        )
        guestManager.addContainer(info).lastResult().bind()
        return info
    }

    context(_: Raise<Failure>)
    private suspend fun getContainers() = guestManager.getContainers()

    context(_: Raise<Failure>)
    private suspend fun getNetworks() = guestManager.getNetworks()

    context(_: Raise<Failure>)
    private suspend fun addTestPortForwarding() = guestManager.addPortForwarding(
        id = TEST_CONTAINER_ID,
        internalPort = fwd.internalPort,
        externalPort = fwd.externalPort,
        protocol = fwd.protocol,
    )

    context(_: Raise<Failure>)
    private suspend fun addTestNetwork(name: String = TEST_NETWORK_NAME) = guestManager.addNetwork(name)

    context(_: Raise<Failure>)
    private suspend fun ContainerInfo.getContainer() = getContainers().first { it.id == id }

    context(_: Raise<Failure>)
    private suspend fun getInfo(): AgentInfo = guestManager.getInfo()

    @AfterTest
    fun stopTest() = runIntervirtTest {
        guestManager.wipe().lastResult().bind()
        guestManager.close()
        stopKoin()
    }
}
