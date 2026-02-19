package io.github.bommbomm34.intervirt.core.api.intervirtos

import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.core.withCatchingContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlin.io.path.*

class DnsServerManager(
    osClient: IntervirtOSClient
) {
    private val logger = KotlinLogging.logger {  }
    private val client = osClient.getClient()
    private val ioClient = client.ioClient
    val docker = client.docker

    private var id: String? = null

    suspend fun init(): Result<String> = withCatchingContext(Dispatchers.IO) {
        logger.debug { "Initializing DNS server manager" }
        val potentialId = docker.getContainer("apache2").getOrThrow()
        potentialId?.let { return@withCatchingContext it }
        // Create new container
        val hostPath = ioClient.getPath("/opt/intervirt/coredns")
            .createParentDirectories()
            .createDirectory()
        val newId = docker.addContainer(
            name = "coredns",
            image = "coredns/coredns",
            portForwardings = listOf(PortForwarding("tcp", 80, 80)),
            volumes = mapOf(hostPath.absolutePathString() to "/etc/coredns")
        ).getOrThrow()
        // TODO: Setup CoreDNS
        id = newId
        newId
    }

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

    suspend fun listRecords(record: DnsRecord): Result<List<DnsRecord>> = withCatchingContext(Dispatchers.IO){
        getMainFile()
            .readText()
            .lines()
            .map { DnsRecord.parse(it) }
    }

    suspend fun restart(): Result<Unit> = docker.restartContainer(getId())

    private fun getId(): String {
        val idClone = id
        require(idClone != null) { "HTTP server manager isn't initialized" }
        return idClone
    }

    private fun getMainFile() = ioClient.getPath("/opt/intervirt/coredns/main.local")
}