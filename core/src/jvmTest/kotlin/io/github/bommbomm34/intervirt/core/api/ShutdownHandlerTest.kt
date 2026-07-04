/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.raise.Raise
import io.github.bommbomm34.intervirt.core.api.impl.DefaultExecutor
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.core.singleProject
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.runIntervirtTest
import io.github.bommbomm34.intervirt.secret.SecretService
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.*

class ShutdownHandlerTest : KoinTest {
    private val _guestManager: GuestManager by inject()
    private val guestManager: MockGuestManager
        get() = _guestManager as MockGuestManager
    private val shutdownHandler: ShutdownHandler by inject()

    @BeforeTest
    fun start(){
        startKoin {
            modules(
                module {
                    single<DefaultExecutor>() bind Executor::class
                    single<Downloader>()
                    single<GuestManager> { MockGuestManager() }
                    single<DeviceManager>()
                    single<FileManager>()
                    single<QemuClient>()
                    single<ShutdownHandler>()
                    single {
                        SecretService(
                            serviceName = "io.github.bommbomm34.intervirt",
                            logger = get<AppEnv>().getLogger(SecretService::class)
                        )
                    }
                    single { getTestAppEnv() }
                    single { getHttpClient() }
                    singleProject()
                }
            )
        }
    }

    @Test
    fun shouldGracefulShutdown() = runIntervirtTest {
        shutdownHandler.gracefulShutdown()
        assertEquals(true, shutdownHandler.closed)
    }

    @Test
    fun shouldNotDoubleClose() = runIntervirtTest {
        shutdownHandler.gracefulShutdown()
        assertEquals(1, guestManager.closed)
        shutdownHandler.gracefulShutdown() // Close a second time
        assertEquals(1, guestManager.closed)
    }

    @Test
    fun shouldGenerateReport() = runIntervirtTest {
        val thread = Thread.currentThread()
        val throwable = IllegalStateException("Just some random exception by tests")
        val (report, log) = shutdownHandler.generateCrashReport(
            throwable = throwable,
            threadName = thread.name,
            writeToReportFile = false,
            writeToLogFile = false
        )
        assertContains(report, "Timestamp")
        assertContains(report, "IllegalStateException")
        assertContains(log, "stdout")
        assertContains(log, "stderr")
    }


    @AfterTest
    fun stop(){
        stopKoin()
    }
}

private class MockGuestManager : GuestManager by VirtualGuestManager() {
    var closed = 0

    context(_: Raise<Failure>)
    override suspend fun close() {
        closed++
    }
}
