/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.api.impl.AtomicAppEnvHolder
import io.github.bommbomm34.intervirt.core.api.impl.DefaultExecutor
import io.github.bommbomm34.intervirt.core.data.OS
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.data.getOS
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.core.util.runIntervirtTest
import io.github.vinceglb.filekit.PlatformFile
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ExecutorTest {
    val executor = DefaultExecutor(AtomicAppEnvHolder(getTestAppEnv()))

    @Test
    fun shouldRunSuccessfulCommandOnHost() = runIntervirtTest {
        val (output, status) = executor
            .runCommand(null, listOf(*getEchoPath(), "Hello World"))
            .getCommandResult()
        assertEquals(0, status)
        assertContains(output, "Hello World")
    }

    @Test
    fun shouldRunSuccessfulCommandOnHostWithWorkingFolder() = runIntervirtTest {
        val testFolder = when (getOS()) {
            OS.WINDOWS -> PlatformFile("C:\\Windows\\System32\\drivers\\etc\\")
            OS.LINUX -> PlatformFile("/etc/")
        }

        val (_, status) = executor
            .runCommand(testFolder, listOf(*getCatPath(), "hosts"))
            .getCommandResult()
        assertEquals(0, status)
    }

    @Test
    fun shouldRunNotExistingCommandOnHost() = runIntervirtTest {
        val (_, status) = executor
            .runCommand(null, listOf("invalid_command_intervirt_${UUID.randomUUID().hashCode()}"))
            .getCommandResult()
        assertNotEquals(0, status)
    }

    private fun getCatPath(): Array<String> = when (getOS()) {
        OS.WINDOWS -> arrayOf("C:\\Windows\\System32\\cmd.exe", "type")
        OS.LINUX -> arrayOf("/usr/bin/cat")
    }

    private fun getEchoPath(): Array<String> = when (getOS()) {
        OS.WINDOWS -> arrayOf("C:\\Windows\\System32\\cmd.exe", "echo")
        OS.LINUX -> arrayOf("/usr/bin/echo")
    }
}
