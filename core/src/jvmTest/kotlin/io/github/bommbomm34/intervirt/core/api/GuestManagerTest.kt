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
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.core.singleAppEnvHolder
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
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.ktor.client.*
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

class GuestManagerTest : KoinTest {
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
    fun `should add container`() = runIntervirtTest {
        val container = addTestContainer()
        getContainers() shouldContain container
    }

    @Test
    fun `should remove container`() = runIntervirtTest {
        val container = addTestContainer()
        guestManager.removeContainer(TEST_CONTAINER_ID).lastResult().bind()
        getContainers() shouldNotContain container
    }

    @Test
    fun `should set IPv4 of container`() = runIntervirtTest {
        val container = addTestContainer()
        val newIP = randomIpv4(getInfo().ipv4Subnet)
        guestManager.setIpv4(
            id = TEST_CONTAINER_ID,
            newIP = newIP,
        )
        newIP shouldBeEqual container.getContainer().ipv4
    }

    @Test
    fun `should set IPv6 of container`() = runIntervirtTest {
        val container = addTestContainer()
        val newIP = randomIpv6(getInfo().ipv6Subnet)
        guestManager.setIpv6(
            id = TEST_CONTAINER_ID,
            newIP = newIP,
        )
        newIP shouldBeEqual container.getContainer().ipv6
    }

    @Test
    fun `should connect container with network`() = runIntervirtTest {
        addTestContainer()
        addTestNetwork()
        guestManager.connect(TEST_CONTAINER_ID, TEST_NETWORK_NAME)
        getNetworks()[TEST_NETWORK_NAME]!! shouldContain TEST_CONTAINER_ID
    }

    @Test
    fun `should disconnect container from network`() = runIntervirtTest {
        addTestContainer()
        addTestNetwork()
        guestManager.connect(TEST_CONTAINER_ID, TEST_NETWORK_NAME)
        guestManager.disconnect(TEST_CONTAINER_ID, TEST_NETWORK_NAME)
        getNetworks()[TEST_NETWORK_NAME]!! shouldNotContain TEST_CONTAINER_ID
    }

    @Test
    fun `should enable internet access`() = runIntervirtTest {
        val container = addTestContainer()
        guestManager.setInternetAccess(TEST_CONTAINER_ID, true)
        container.getContainer().internet.shouldBeTrue()
    }

    @Test
    fun `should add port forwarding`() = runIntervirtTest {
        val container = addTestContainer()
        addTestPortForwarding()
        container.getContainer().portForwardings shouldContain FWD
    }

    @Test
    fun `should remove port forwarding`() = runIntervirtTest {
        val container = addTestContainer()
        addTestPortForwarding()
        guestManager.removePortForwarding(TEST_CONTAINER_ID, FWD.externalPort, FWD.protocol)
        container.getContainer().portForwardings shouldNotContain FWD
    }

    @Test
    fun `should start container`() = runIntervirtTest {
        val container = addTestContainer()
        guestManager.stopContainer(TEST_CONTAINER_ID) // Containers start by default
        guestManager.startContainer(TEST_CONTAINER_ID)
        container.getContainer().running.shouldBeTrue()
    }

    @Test
    fun `should stop container`() = runIntervirtTest {
        val container = addTestContainer()
        guestManager.stopContainer(TEST_CONTAINER_ID)
        container.getContainer().running.shouldBeFalse()
    }

    @Test
    fun `should wipe`() = runIntervirtTest {
        val container = addTestContainer()
        addTestNetwork()
        val progress = guestManager.wipe().toList()
        progress shouldContain ResultProgress.success(Unit)
        getContainers() shouldNotContain container
        getNetworks() shouldNotContainKey TEST_NETWORK_NAME
    }

    @Test
    fun `should update`() = runIntervirtTest {
        val progress = guestManager.update().toList()
        progress shouldContain ResultProgress.success(Unit)
    }

    @Test
    fun `should get info`() = runIntervirtTest {
        val info = getInfo()
        if (guestManager is VirtualGuestManager) info.version shouldBeEqual CURRENT_VERSION
    }

    @Test
    fun `should get containers`() = runIntervirtTest {
        val container1 = addTestContainer(DeviceId("computer-10002"))
        val container2 = addTestContainer(DeviceId("computer-10003"))
        val container3 = addTestContainer(DeviceId("computer-10005"))
        val containers = getContainers()
        containers shouldContain container1
        containers shouldContain container2
        containers shouldContain container3
    }

    @Test
    fun `should add network`() = runIntervirtTest {
        addTestNetwork()
        getNetworks() shouldContainKey TEST_NETWORK_NAME
    }

    @Test
    fun `should remove networks`() = runIntervirtTest {
        addTestNetwork()
        guestManager.removeNetwork(TEST_NETWORK_NAME)
        getNetworks() shouldNotContainKey TEST_NETWORK_NAME
    }

    @Test
    fun `should get networks`() = runIntervirtTest {
        addTestNetwork("test-network1")
        addTestNetwork("test-network2")
        addTestNetwork("test-network3")
        val networks = getNetworks()
        networks shouldContainKey "test-network1"
        networks shouldContainKey "test-network2"
        networks shouldContainKey "test-network3"
    }

    @Test
    fun `should close GuestManager`() = runIntervirtTest {
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
        internalPort = FWD.internalPort,
        externalPort = FWD.externalPort,
        protocol = FWD.protocol,
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

    companion object {
        val FWD = PortForwarding(
            protocol = "tcp",
            internalPort = 22,
            externalPort = 2222,
        )

        val TEST_CONTAINER_ID = DeviceId("computer-10001")

        const val TEST_NETWORK_NAME = "test-network"
    }
}
