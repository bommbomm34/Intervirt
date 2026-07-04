/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.intervirtos

import arrow.core.raise.context.Raise
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerBasedManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.data.Failure

import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.core.util.ext.withCatchingContext
import kotlinx.coroutines.Dispatchers
import kotlin.io.path.appendLines
import kotlin.io.path.readText
import kotlin.io.path.writeText

class DnsServerManager(
    appEnv: AppEnv,
    osClient: IntervirtOSClient,
) : DockerBasedManager(
    appEnv = appEnv,
    osClient = osClient,
    containerName = "coredns",
    containerImage = "coredns/coredns",
    portForwardings = listOf(
        PortForwarding("tcp", 53, 53),
        PortForwarding("udp", 53, 53),
    ),
    volumes = mapOf("./" to "/etc/coredns"),
) {
    private val ioClient = client.ioClient
    val docker = client.docker

    context(_: Raise<Failure>)
    suspend fun addRecord(record: DnsRecord) = withCatchingContext(Dispatchers.IO) {
        getMainFile().appendLines(listOf(record.toString()))
        restart()
    }

    context(_: Raise<Failure>)
    suspend fun removeRecord(record: DnsRecord) = withCatchingContext(Dispatchers.IO) {
        val main = getMainFile()
        val content = main.readText()
        val new = content
            .lines()
            .filterNot { it.trim().equals(record.toString(), true) }
            .joinToString("\n")
        main.writeText(new)
        restart()
    }

    context(_: Raise<Failure>)
    suspend fun listRecords(): List<DnsRecord> = withCatchingContext(Dispatchers.IO) {
        getMainFile()
            .readText()
            .lines()
            .map(DnsRecord::parse)
    }

    context(_: arrow.core.raise.Raise<Failure>)
    suspend fun restart() = docker.restartContainer(id)

    private fun getMainFile() = ioClient.getPath("/opt/intervirt/coredns/main.local")
}
