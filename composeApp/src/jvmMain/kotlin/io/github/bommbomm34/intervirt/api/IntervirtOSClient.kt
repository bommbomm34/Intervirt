package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.data.Address
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.data.dns.DnsResolverOutput
import io.github.bommbomm34.intervirt.data.getTotalCommandStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
}

class IntervirtOSClient(
    private val ioClient: ContainerIOClient,
    private val computer: Device.Computer
) {
    private val logger = KotlinLogging.logger { }
    private val serviceManager = SystemServiceManager(ioClient)

    suspend fun lookupDns(
        name: String,
        type: String,
        nameserver: String,
        reverse: Boolean
    ): List<DnsRecord> {
        val baseCommandList = listOf("doggo", name, "--type", type, "--nameserver", nameserver, "--json")
        val commandList = if (reverse) baseCommandList + "-x" else baseCommandList
        logger.debug { "Execute command \"${commandList.joinToString(" ")}\" for DNS lookup" }
        return ioClient.exec(
            commands = commandList
        ).fold(
            onSuccess = { flow ->
                val total = flow.getTotalCommandStatus()
                val output = total.message!!
                logger.debug { "Received during DNS lookup:\n$output" }
                if (total.statusCode!! == 0){
                    val resolverOutput = json.decodeFromString<DnsResolverOutput>(output)
                    resolverOutput.responses
                        .getOrNull(0)
                        ?.answers
                        ?.map { it.toDnsRecord() }
                        ?: emptyList()
                } else {
                    logger.error { "Unexpected status code during DNS lookup: ${total.statusCode}" }
                    emptyList()
                }
            },
            onFailure = { emptyList() }
        )
    }

    suspend fun getProxyUrl(
        deviceManager: DeviceManager
    ): Result<Address> {
        val port = getFreePort()
        return deviceManager.addPortForwarding(
            device = computer,
            internalPort = 1080,
            externalPort = port,
            protocol = "tcp"
        ).map { Address("127.0.0.1", port) }
    }

    suspend fun enableHttpServer(
        enabled: Boolean
    ): Result<Unit> {
        return if (enabled) serviceManager.start("apache2") else serviceManager.stop("apache2")
    }

    suspend fun enableSshServer(
        enabled: Boolean
    ): Result<Unit> {
        return if (enabled) serviceManager.start("ssh") else serviceManager.stop("ssh")
    }
}