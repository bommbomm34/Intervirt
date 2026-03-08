/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.OS
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.data.getOS
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import kotlinx.coroutines.test.runTest
import java.io.File
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ExecutorTest {
    val executor = Executor(getTestAppEnv())

    @Test
    fun shouldRunSuccessfulCommandOnHost() = runTest {
        val (output, status) = executor
            .runCommandOnHost(null, listOf(*getEchoPath(), "Hello World"))
            .getCommandResult()
        assertEquals(0, status)
        assertContains(output, "Hello World")
    }

    @Test
    fun shouldRunSuccessfulCommandOnHostWithWorkingFolder() = runTest {
        val testFolder = when (getOS()){
            OS.WINDOWS -> File("C:\\Windows\\System32\\drivers\\etc\\")
            OS.LINUX -> File("/etc/")
        }

        val (_, status) = executor
            .runCommandOnHost(testFolder, listOf(*getCatPath(), "hosts"))
            .getCommandResult()
        assertEquals(0, status)
    }

    @Test
    fun shouldRunNotExistingCommandOnHost() = runTest {
        val (_, status) = executor
            .runCommandOnHost(null, listOf("invalid_command_intervirt_${UUID.randomUUID().hashCode()}"))
            .getCommandResult()
        assertNotEquals(0, status)
    }

    private fun getCatPath(): Array<String> = when (getOS()){
        OS.WINDOWS -> arrayOf("C:\\Windows\\System32\\cmd.exe", "type")
        OS.LINUX -> arrayOf("/usr/bin/cat")
    }

    private fun getEchoPath(): Array<String> = when (getOS()){
        OS.WINDOWS -> arrayOf("C:\\Windows\\System32\\cmd.exe", "echo")
        OS.LINUX -> arrayOf("/usr/bin/echo")
    }
}