/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.io.github.bommbomm34.intervirt.logging

import io.github.bommbomm34.intervirt.logging.KLogger
import io.github.bommbomm34.intervirt.logging.LogLevel
import io.github.bommbomm34.intervirt.logging.OutputStream
import io.github.bommbomm34.intervirt.logging.getDefaultStream
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

const val TEST_LINE = "Hello, this is a test!"

class KLoggerTest {

    @Test
    fun shouldTraceWhenLogLevelIsTrace() {
        val (logger, stream) = getLogger(LogLevel.TRACE)
        logger.trace { TEST_LINE }
        assertContains(stream.stdoutLast(), TEST_LINE)
    }

    @Test
    fun shouldDebug() {
        val (logger, stream) = getLogger(LogLevel.DEBUG)
        logger.debug { TEST_LINE }
        assertContains(stream.stdoutLast(), TEST_LINE)
    }

    @Test
    fun shouldInfo() {
        val (logger, stream) = getLogger(LogLevel.INFO)
        logger.info { TEST_LINE }
        assertContains(stream.stdoutLast(), TEST_LINE)
    }

    @Test
    fun shouldNotDebugWhenLogLevelIsInfo() {
        val (logger, stream) = getLogger(LogLevel.INFO)
        logger.debug { TEST_LINE }
        assertTrue(stream.stdout.isEmpty())
    }

    @Test
    fun shouldErrorToStderr() {
        val (logger, stream) = getLogger(LogLevel.ERROR)
        logger.error { TEST_LINE }
        assertContains(stream.stderrLast(), TEST_LINE)
    }

    @Test
    fun shouldNotTraceWhenLogLevelIsDebug() {
        val (logger, stream) = getLogger(LogLevel.DEBUG)
        logger.trace { TEST_LINE }
        assertTrue(stream.stdout.isEmpty())
    }

    private fun getLogger(level: LogLevel): Pair<KLogger, MockStream> {
        val mockStream = MockStream()

        return KLogger(
            name = "KLoggerTest",
            level = level,
            streams = arrayOf(mockStream),
        ) to mockStream
    }
}

private class MockStream : OutputStream {
    private val defaultStream = getDefaultStream()
    override val colorSupported = defaultStream.colorSupported

    val stdout = mutableListOf<String>()
    val stderr = mutableListOf<String>()

    override fun println(line: String) {
        stdout.add(line)
        defaultStream.println(line)
    }

    override fun printlnErr(line: String) {
        stderr.add(line)
        defaultStream.printlnErr(line)
    }

    fun stdoutLast() = stdout.last()

    fun stderrLast() = stderr.last()
}