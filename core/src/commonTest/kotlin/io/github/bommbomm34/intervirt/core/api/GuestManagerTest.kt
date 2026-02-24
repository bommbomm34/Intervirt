package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.CURRENT_VERSION
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.binds
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails

private const val TEST_CONTAINER_ID = "my-id"

class GuestManagerTest : KoinTest {
    val guestManager: GuestManager by inject()

    @BeforeTest
    fun startTest() {
        startKoin {
            modules(
                module {
                    singleOf(::VirtualGuestManager).binds(arrayOf(GuestManager::class))
                },
            )
        }
    }

    @Test
    fun testAddContainer() = runTest {
        addTestContainer()
    }

    @Test
    fun testRemoveContainer() = runTest {
        addTestContainer()
        guestManager.removeContainer(TEST_CONTAINER_ID).getOrThrow()
    }

    @Test
    fun testSetIpv4() = runTest {
        addTestContainer()
        guestManager.setIpv4(
            id = TEST_CONTAINER_ID,
            newIP = "0.1.0.1",
        ).getOrThrow()
    }

    @Test
    fun testSetIpv6() = runTest {
        addTestContainer()
        guestManager.setIpv6(
            id = TEST_CONTAINER_ID,
            newIP = "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff",
        ).getOrThrow()
    }

    @Test
    fun testConnect() = runTest {
        addTestContainer("id1")
        addTestContainer("id2")
        guestManager.connect("id1", "id2").getOrThrow()
    }

    @Test
    fun testDisconnect() = runTest {
        addTestContainer("id1")
        addTestContainer("id2")
        guestManager.connect("id1", "id2").getOrThrow()
        guestManager.disconnect("id1", "id2").getOrThrow()
    }

    @Test
    fun testSetInternetAccess() = runTest {
        addTestContainer()
        guestManager.setInternetAccess(TEST_CONTAINER_ID, true).getOrThrow()
    }

    @Test
    fun testAddPortForwarding() = runTest {
        addTestContainer()
        addTestPortForwarding()
    }

    @Test
    fun testRemovePortForwarding() = runTest {
        addTestContainer()
        addTestPortForwarding()
        guestManager.removePortForwarding(2222, "tcp").getOrThrow()
    }

    @Test
    fun testStartContainer() = runTest {
        addTestContainer()
        guestManager.startContainer(TEST_CONTAINER_ID).getOrThrow()
    }

    @Test
    fun testStopContainer() = runTest {
        addTestContainer()
        guestManager.stopContainer(TEST_CONTAINER_ID).getOrThrow()
    }

    @Test
    fun testWipe() = runTest {
        val progress = guestManager.wipe().toList()
        assertContains(progress, ResultProgress.success(Unit))
    }

    @Test
    fun testUpdate() = runTest {
        val progress = guestManager.update().toList()
        assertContains(progress, ResultProgress.success(Unit))
    }

    @Test
    fun testShutdown() = runTest {
        assertFails { guestManager.shutdown().getOrThrow() }
    }

    @Test
    fun testReboot() = runTest {
        guestManager.reboot().getOrThrow()
    }

    @Test
    fun testGetVersion() = runTest {
        val version = guestManager.getVersion().getOrThrow()
        assertEquals(CURRENT_VERSION, version)
    }

    @Test
    fun testClose() = runTest {
        guestManager.close().getOrThrow()
    }

    private suspend fun addTestContainer(id: String = TEST_CONTAINER_ID) = guestManager.addContainer(
        id = id,
        initialIpv4 = "0.0.0.0",
        initialIpv6 = "::1",
        mac = "ff:ff:ff:ff:ff:ff",
        internet = false,
        image = "my-image",
    ).getOrThrow()

    private suspend fun addTestPortForwarding() = guestManager.addPortForwarding(
        id = TEST_CONTAINER_ID,
        internalPort = 22,
        externalPort = 2222,
        protocol = "tcp",
    ).getOrThrow()

    @AfterTest
    fun stopTest() {
        stopKoin()
    }
}