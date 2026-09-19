/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.io.github.bommbomm34.intervirt.logging

import io.github.bommbomm34.intervirt.logging.KLogger
import io.github.bommbomm34.intervirt.logging.LogLevel
import io.github.bommbomm34.intervirt.logging.OutputStream
import io.github.bommbomm34.intervirt.logging.debug
import io.github.bommbomm34.intervirt.logging.error
import io.github.bommbomm34.intervirt.logging.info
import io.github.bommbomm34.intervirt.logging.trace
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class KLoggerTest {

    @Test
    fun `should log TRACE when log level is TRACE`() {
        val (logger, stream) = getLogger(LogLevel.TRACE)
        logger.trace { TEST_LINE }
        stream.stdoutLast() shouldContain TEST_LINE
    }

    @Test
    fun `should log DEBUG`() {
        val (logger, stream) = getLogger(LogLevel.DEBUG)
        logger.debug { TEST_LINE }
        stream.stdoutLast() shouldContain TEST_LINE
    }

    @Test
    fun `should log INFO`() {
        val (logger, stream) = getLogger(LogLevel.INFO)
        logger.info { TEST_LINE }
        stream.stdoutLast() shouldContain TEST_LINE
    }

    @Test
    fun `should not log DEBUG when log level is INFO`() {
        val (logger, stream) = getLogger(LogLevel.INFO)
        logger.debug { TEST_LINE }
        stream.stdout.shouldBeEmpty()
    }

    @Test
    fun `should log ERROR to stderr`() {
        val (logger, stream) = getLogger(LogLevel.ERROR)
        logger.error { TEST_LINE }
        stream.stderrLast() shouldContain TEST_LINE
    }

    @Test
    fun `should not log TRACE when log level is DEBUG`() {
        val (logger, stream) = getLogger(LogLevel.DEBUG)
        logger.trace { TEST_LINE }
        stream.stdout.shouldBeEmpty()
    }

    private fun getLogger(level: LogLevel): Pair<KLogger, MockStream> {
        val mockStream = MockStream()

        return KLogger(
            name = "KLoggerTest",
            level = level,
            streams = arrayOf(mockStream),
        ) to mockStream
    }

    companion object {
        const val TEST_LINE = "Hello, this is a test!"
    }
}

private class MockStream : OutputStream {
    private val defaultStream get() = OutputStream.DEFAULT
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
