/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.raise.Raise
import io.github.bommbomm34.intervirt.core.api.impl.DefaultExecutor
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.core.singleAppEnvHolder
import io.github.bommbomm34.intervirt.core.singleProject
import io.github.bommbomm34.intervirt.core.util.ignoreFailure
import io.github.bommbomm34.intervirt.core.util.runIntervirtTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
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
        portForwardings = listOf(),
    )
    var path: Path? = null

    @BeforeTest
    fun startTest() {
        startKoin {
            modules(
                module {
                    single<DeviceManager>()
                    single<GuestManager> { VirtualGuestManager() }
                    single<QemuClient>()
                    single<DefaultExecutor>() bind Executor::class
                    single<FileManager>()
                    singleAppEnvHolder()
                    singleProject()
                    single { getHttpClient() }
                },
            )
        }
    }

    @Test
    fun shouldGetIOClient() = runIntervirtTest {
        val device = createDevice()
        ignoreFailure { deviceManager.getIOClient(device) }
    }

    @Test
    fun shouldExec() = runIntervirtTest {
        val device = createDevice()
        val ioClient = deviceManager.getIOClient(device)
        val res = ioClient.exec(listOf("echo", "Hello World")).getCommandResult()
        assertEquals(0, res.statusCode)
        assertContains(res.output, "Hello World")
    }

    @Test
    fun shouldWriteFile() = runIntervirtTest {
        val device = createDevice()
        val ioClient = deviceManager.getIOClient(device)
        ioClient.getTestPath().writeText("Hello Test!")
    }

    @Test
    fun shouldReadFile() = runIntervirtTest {
        val device = createDevice()
        val ioClient = deviceManager.getIOClient(device)
        val path = ioClient.getTestPath()
        path.writeText("Hello")
        assertEquals("Hello", path.readText())
    }

    @Test
    fun shouldCloseClient() = runIntervirtTest {
        val device = createDevice()
        val ioClient = deviceManager.getIOClient(device)
        ioClient.close()
    }

    context(_: Raise<Failure>)
    private suspend fun createDevice(): Device.Computer = deviceManager.addComputer(mockComputer)

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
