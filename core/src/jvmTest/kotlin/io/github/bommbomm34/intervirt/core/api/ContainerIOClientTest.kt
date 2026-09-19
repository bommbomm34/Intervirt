/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.raise.Raise
import io.github.bommbomm34.intervirt.core.api.impl.DefaultExecutor
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.DeviceId
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.singleAppEnvHolder
import io.github.bommbomm34.intervirt.core.singleProjectHolder
import io.github.bommbomm34.intervirt.core.util.ignoreFailure
import io.github.bommbomm34.intervirt.core.util.randomIpv4
import io.github.bommbomm34.intervirt.core.util.randomIpv6
import io.github.bommbomm34.intervirt.core.util.randomMac
import io.github.bommbomm34.intervirt.core.util.runIntervirtTest
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.flow.collect
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
    lateinit var mockComputer: Device.Computer

    val deviceManager: DeviceManager by inject()
    val guestManager: GuestManager by inject()
    var path: Path? = null

    @BeforeTest
    fun startTest() = runIntervirtTest {
        startKoin {
            modules(
                module {
                    single<DeviceManager>()
                    single<GuestManager> { VirtualGuestManager() }
                    single<QemuClient>()
                    single<DefaultExecutor>() bind Executor::class
                    single<FileManager>()
                    singleAppEnvHolder()
                    singleProjectHolder()
                    single { getHttpClient() }
                },
            )
        }

        val info = guestManager.getInfo()
        mockComputer = Device.Computer(
            id = DeviceId("computer-10000"),
            image = "debian/13",
            name = "Mock Computer",
            x = 0,
            y = 0,
            ipv4 = randomIpv4(info.ipv4Subnet),
            ipv6 = randomIpv6(info.ipv6Subnet),
            mac = randomMac(),
            internetEnabled = false,
            portForwardings = emptyList(),
        )
    }

    @Test
    fun `should get IO client`() = runIntervirtTest {
        val device = createDevice()
        deviceManager.getIOClient(device)
    }

    @Test
    fun `should execute`() = runIntervirtTest {
        val device = createDevice()
        val ioClient = deviceManager.getIOClient(device)
        val res = ioClient.exec(listOf("echo", "Hello World")).getCommandResult()
        res.statusCode shouldBeEqual 0
        res.output shouldContain "Hello World"
    }

    @Test
    fun `should write file`() = runIntervirtTest {
        val device = createDevice()
        val ioClient = deviceManager.getIOClient(device)
        ioClient.getTestPath().writeText("Hello Test!")
    }

    @Test
    fun `should read file`() = runIntervirtTest {
        val device = createDevice()
        val ioClient = deviceManager.getIOClient(device)
        val path = ioClient.getTestPath()
        path.writeText("Hello")
        path.readText() shouldBeEqual "Hello"
    }

    @Test
    fun `should close client`() = runIntervirtTest {
        val device = createDevice()
        val ioClient = deviceManager.getIOClient(device)
        ioClient.close()
    }

    context(_: Raise<Failure>)
    private suspend fun createDevice(): Device.Computer =
        deviceManager.addComputer(mockComputer).let { result ->
            result.flow.collect()
            result.device
        }

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
