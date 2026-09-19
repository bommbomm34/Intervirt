/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.raise.Raise
import io.github.bommbomm34.intervirt.core.api.atomic.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.impl.DefaultExecutor
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.singleAppEnvHolder
import io.github.bommbomm34.intervirt.core.singleProjectHolder
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.runIntervirtTest
import io.github.bommbomm34.intervirt.secret.SecretService
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.string.shouldContain
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
    fun start() {
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
                            logger = get<AppEnvHolder>().get().getLogger(SecretService::class),
                        )
                    }
                    singleAppEnvHolder()
                    single { getHttpClient() }
                    singleProjectHolder()
                },
            )
        }
    }

    @Test
    fun `should graceful shutdown`() = runIntervirtTest {
        shutdownHandler.gracefulShutdown()
        shutdownHandler.closed.shouldBeTrue()
    }

    @Test
    fun `should not close twice`() = runIntervirtTest {
        shutdownHandler.gracefulShutdown()
        guestManager.closed shouldBeExactly 1
        shutdownHandler.gracefulShutdown() // Close a second time
        guestManager.closed shouldBeExactly 1
    }

    @Test
    fun `should generate valid report`() = runIntervirtTest {
        val thread = Thread.currentThread()
        val throwable = IllegalStateException("Just some random exception by tests")
        val (report, log) = shutdownHandler.generateCrashReport(
            throwable = throwable,
            threadName = thread.name,
            writeToReportFile = false,
            writeToLogFile = false,
        )
        report shouldContain "Timestamp"
        report shouldContain "IllegalStateException"
        log shouldContain "stdout"
        log shouldContain "stderr"
    }


    @AfterTest
    fun stop() {
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
