package io.github.bommbomm34.intervirt.core.api.intervirtos

import io.github.bommbomm34.intervirt.core.api.DockerBasedManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.core.withCatchingContext
import kotlinx.coroutines.Dispatchers
import kotlin.io.path.*

class DnsServerManager(
    osClient: IntervirtOSClient
) : DockerBasedManager(
    osClient = osClient,
    containerName = "coredns",
    containerImage = "coredns/coredns",
    portForwardings = listOf(
        PortForwarding("tcp", 53, 53),
        PortForwarding("udp", 53, 53),
    ),
    bind = "/etc/coredns/"
) {
    private val ioClient = client.ioClient
    val docker = client.docker

    suspend fun addRecord(record: DnsRecord): Result<Unit> = withCatchingContext(Dispatchers.IO) {
        getMainFile().appendLines(listOf(record.toString()))
        restart().getOrThrow()
    }

    suspend fun removeRecord(record: DnsRecord): Result<Unit> = withCatchingContext(Dispatchers.IO) {
        val main = getMainFile()
        val content = main.readText()
        val new = content
            .lines()
            .filterNot { it.trim().equals(record.toString(), true) }
            .joinToString("\n")
        main.writeText(new)
        restart().getOrThrow()
    }

    suspend fun listRecords(): Result<List<DnsRecord>> = withCatchingContext(Dispatchers.IO){
        getMainFile()
            .readText()
            .lines()
            .map { DnsRecord.parse(it) }
    }

    suspend fun restart(): Result<Unit> = docker.restartContainer(id)

    private fun getMainFile() = ioClient.getPath("/opt/intervirt/coredns/main.local")
}