package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.CURRENT_VERSION
import io.github.bommbomm34.intervirt.core.api.impl.AgentGuestManager
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.agent.ContainerInfo
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.util.randomIpv4
import io.github.bommbomm34.intervirt.core.util.randomIpv6
import io.github.bommbomm34.intervirt.core.util.randomMac
import io.ktor.client.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.*

private const val TEST_CONTAINER_ID = "my-id"

class GuestManagerTest : KoinTest {
    val fwd = PortForwarding(
        protocol = "tcp",
        internalPort = 22,
        externalPort = 2222,
    )
    val guestManager: GuestManager by inject()

    @BeforeTest
    fun startTest() {
        val appEnv = getTestAppEnv()
        startKoin {
            modules(
                module {
                    if (appEnv.VIRTUAL_AGENT_MODE){
                        single<GuestManager> { VirtualGuestManager() }
                    } else {
                        single<AppEnv> { appEnv }
                        single<HttpClient> { getHttpClient() }
                        single<GuestManager> { AgentGuestManager(get(), get()) }
                    }
                },
            )
        }
    }

    @Test
    fun shouldAddContainer() = runTest {
        val container = addTestContainer()
        assertContains(getContainers(), container)
    }

    @Test
    fun shouldRemoveContainer() = runTest {
        val container = addTestContainer()
        guestManager.removeContainer(TEST_CONTAINER_ID).getOrThrow()
        assertFalse { getContainers().contains(container) }
    }

    @Test
    fun shouldSetIpv4() = runTest {
        val container = addTestContainer()
        val newIP = "192.168.144.189"
        guestManager.setIpv4(
            id = TEST_CONTAINER_ID,
            newIP = newIP,
        ).getOrThrow()
        assertEquals(newIP, container.getContainer().ipv4)
    }

    @Test
    fun shouldSetIpv6() = runTest {
        val container = addTestContainer()
        val newIP = "fd09:cc44:0af9:e495:1463:5f83:ad54:260b"
        guestManager.setIpv6(
            id = TEST_CONTAINER_ID,
            newIP = newIP,
        ).getOrThrow()
        assertEquals(newIP, container.getContainer().ipv6)
    }

    @Test
    fun shouldConnect() = runTest {
        addTestContainer("id1")
        addTestContainer("id2")
        guestManager.connect("id1", "id2").getOrThrow()
    }

    @Test
    fun shouldDisconnect() = runTest {
        addTestContainer("id1")
        addTestContainer("id2")
        guestManager.connect("id1", "id2").getOrThrow()
        guestManager.disconnect("id1", "id2").getOrThrow()
    }

    @Test
    fun shouldSetInternetAccess() = runTest {
        val container = addTestContainer()
        guestManager.setInternetAccess(TEST_CONTAINER_ID, true).getOrThrow()
        assertTrue { container.getContainer().internet }
    }

    @Test
    fun shouldAddPortForwarding() = runTest {
        val container = addTestContainer()
        addTestPortForwarding()
        assertContains(container.getContainer().portForwardings, fwd)
    }

    @Test
    fun shouldRemovePortForwarding() = runTest {
        val container = addTestContainer()
        addTestPortForwarding()
        guestManager.removePortForwarding(fwd.externalPort, fwd.protocol).getOrThrow()
        assertFalse { container.getContainer().portForwardings.contains(fwd) }
    }

    @Test
    fun shouldStartContainer() = runTest {
        val container = addTestContainer()
        guestManager.startContainer(TEST_CONTAINER_ID).getOrThrow()
        assertTrue { container.getContainer().running }
    }

    @Test
    fun shouldStopContainer() = runTest {
        val container = addTestContainer()
        guestManager.stopContainer(TEST_CONTAINER_ID).getOrThrow()
        assertFalse { container.getContainer().running }
    }

    @Test
    fun shouldWipe() = runTest {
        val progress = guestManager.wipe().toList()
        assertContains(progress, ResultProgress.success(Unit))
    }

    @Test
    fun shouldUpdate() = runTest {
        val progress = guestManager.update().toList()
        assertContains(progress, ResultProgress.success(Unit))
    }

    @Test
    fun shouldShutdown() = runTest {
        assertFails { guestManager.shutdown().getOrThrow() }
    }

    @Test
    fun shouldReboot() = runTest {
        guestManager.reboot().getOrThrow()
    }

    @Test
    fun shouldGetVersion() = runTest {
        val version = guestManager.getVersion().getOrThrow()
        assertEquals(CURRENT_VERSION, version)
    }

    @Test
    fun shouldClose() = runTest {
        guestManager.close().getOrThrow()
    }

    private suspend fun addTestContainer(id: String = TEST_CONTAINER_ID, ): ContainerInfo {
        val info = ContainerInfo(
            id = id,
            ipv4 = randomIpv4(),
            ipv6 = randomIpv6(),
            mac = randomMac(),
            internet = false,
            image = "my-image",
        )
        guestManager.addContainer(info).getOrThrow()
        return info
    }

    private suspend fun getContainers() = guestManager.getContainers().getOrThrow()

    private suspend fun addTestPortForwarding() = guestManager.addPortForwarding(
        id = TEST_CONTAINER_ID,
        internalPort = fwd.internalPort,
        externalPort = fwd.externalPort,
        protocol = fwd.protocol,
    ).getOrThrow()

    private suspend fun ContainerInfo.getContainer() = getContainers().first { it.id == id }

    @AfterTest
    fun stopTest() {
        stopKoin()
    }
}