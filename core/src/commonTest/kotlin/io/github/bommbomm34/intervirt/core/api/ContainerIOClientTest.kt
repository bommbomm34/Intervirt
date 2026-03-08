/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.getAppEnv
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.binds
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.*

class ContainerIOClientTest : KoinTest {
    val deviceManager: DeviceManager by inject()
    val mockComputer = Device.Computer(
        id = "mock-computer",
        image = "debian/13",
        name = "Mock Computer",
        x = 0,
        y = 0,
        ipv4 = "0.0.0.0",
        ipv6 = "::1",
        mac = "ff:ff:ff:ff:ff:ff",
        internetEnabled = false,
        portForwardings = mutableListOf(),
    )
    var path: Path? = null

    @BeforeTest
    fun startTest() {
        startKoin {
            modules(
                module {
                    singleOf(::DeviceManager)
                    single<GuestManager> { VirtualGuestManager() }
                    singleOf(::QemuClient)
                    singleOf(::Executor)
                    singleOf(::FileManager)
                    single { getTestAppEnv() }
                    single { IntervirtConfiguration() }
                    single { getHttpClient() }
                },
            )
        }
    }

    @Test
    fun shouldGetIOClient() = runTest {
        val device = createDevice()
        deviceManager.getIOClient(device).getOrThrow()
    }

    @Test
    fun shouldExec() = runTest {
        val device = createDevice()
        val ioClient = deviceManager.getIOClient(device).getOrThrow()
        val res = ioClient.exec(listOf("echo", "Hello World")).getOrThrow().getCommandResult()
        assertEquals(0, res.statusCode)
        assertContains(res.output, "Hello World")
    }

    @Test
    fun shouldWriteFile() = runTest {
        val device = createDevice()
        val ioClient = deviceManager.getIOClient(device).getOrThrow()
        ioClient.getTestPath().writeText("Hello Test!")
    }

    @Test
    fun shouldReadFile() = runTest {
        val device = createDevice()
        val ioClient = deviceManager.getIOClient(device).getOrThrow()
        val path = ioClient.getTestPath()
        path.writeText("Hello")
        assertEquals("Hello", path.readText())
    }

    @Test
    fun shouldCloseClient() = runTest {
        val device = createDevice()
        val ioClient = deviceManager.getIOClient(device).getOrThrow()
        ioClient.close().getOrThrow()
    }

    private suspend fun createDevice(): Device.Computer = deviceManager.addComputer(mockComputer).getOrThrow()

    private fun ContainerIOClient.getTestPath(): Path {
        path = getPath("/tmp/test.txt")
        path!!.createParentDirectories()
        return path!!
    }

    @AfterTest
    fun stopTest() {
        path?.deleteIfExists()
        stopKoin()
    }
}