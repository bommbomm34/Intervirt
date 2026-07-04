/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.raise.recover
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.totalDiskSpace
import io.github.bommbomm34.intervirt.core.unixTimestamp
import io.github.bommbomm34.intervirt.core.usableDiskSpace
import io.github.bommbomm34.intervirt.core.util.ListOutputStream
import io.github.bommbomm34.intervirt.logging.getDefaultStream
import io.github.bommbomm34.intervirt.secret.SecretService
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.writeString
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.system.exitProcess

@OptIn(ExperimentalAtomicApi::class)
class ShutdownHandler(
    envHolder: AppEnvHolder,
    private val deviceManager: DeviceManager,
    private val guestManager: GuestManager,
    private val qemuClient: QemuClient,
    private val httpClient: HttpClient,
    private val secretService: SecretService,
){
    val appEnv by envHolder
    private var _closed = AtomicBoolean(false)
    val closed get() = _closed.load()

    /**
     * Every exception thrown in [block] will be reported gracefully to the user via [crash].
     * @see crash
     */
    @OptIn(ExperimentalContracts::class)
    inline fun <T> runCrashCatching(block: () -> T): T {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        try {
            return block()
        } catch (e: Throwable) {
            crash(Thread.currentThread(), e)
        }
    }

    /**
     * Shuts all services down gracefully.
     * This method doesn't exit the application.
     * @see gracefulShutdown
     */
    suspend fun gracefulShutdown() {
        if (_closed.compareAndSet(expectedValue = false, newValue = true)){
            recover(
                block = {
                    deviceManager.close()
                    guestManager.close()
                    qemuClient.close()
                },
                recover = {
                    getDefaultStream().printlnErr("Error occurred during closing Intervirt services: $it")
                }
            )
            httpClient.close()
            secretService.close()
        }
    }

    /**
     * This method will stop the application gracefully with a crash report.
     */
    fun crash(thread: Thread, throwable: Throwable): Nothing = runBlocking {
        gracefulShutdown()
        val (report, _) = generateCrashReport(throwable, thread.name)
        getDefaultStream().printlnErr(report)
        exitProcess(1)
    }

    suspend fun generateCrashReport(
        throwable: Throwable,
        threadName: String,
        writeToReportFile: Boolean = true,
        writeToLogFile: Boolean = true,
    ): Pair<String, String> {
        val timestamp = unixTimestamp // The timestamp should be consistent across the report
        val reportFile = PlatformFile("report_$timestamp.txt")
        val logFile = PlatformFile("log_$timestamp.txt")
        val report = """
FATAL CRASH --- Shutting down application...
        
An unexpected error occurred. This seems like an internal error.
It is recommended to report this error: https://github.com/bommbomm34/Intervirt/issues
Please send the log and report file. Don't send personal data!
Two log/report files are written: 
        
- '${logFile.absolutePath()}' which contains the whole log of the application.
- '${reportFile.absolutePath()}' which contains this report and system information.
        
Thread: $threadName
====== STACKTRACE ======
${throwable.stackTraceToString()}
    """.trimIndent()
        val systemInformation = """
Timestamp: $timestamp
Operating system: ${System.getProperty("os.name")}
Architecture: ${System.getProperty("os.arch")}
PID: ${ProcessHandle.current().pid()}
Amount of CPU cores: ${Runtime.getRuntime().availableProcessors()}
Total memory JVM: ${Runtime.getRuntime().totalMemory()}
Free memory JVM: ${Runtime.getRuntime().freeMemory()}
Total disk space: $totalDiskSpace
Usable disk space: $usableDiskSpace
Environment settings of app:

$appEnv
    """.trimIndent()
        val log = """
====== STANDARD OUTPUT (stdout) ======
        
${ListOutputStream.DEFAULT.getStdout().joinToString("\n")}
        
====== STANDARD ERROR (stderr) ======
        
${ListOutputStream.DEFAULT.getStderr().joinToString("\n")}
    """.trimIndent()
        val reportFileContent = "$report\n\n$systemInformation"
        if (writeToReportFile) reportFile.writeString(reportFileContent)
        if (writeToLogFile) logFile.writeString(log)
        return reportFileContent to log
    }
}
