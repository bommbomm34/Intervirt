package io.github.bommbomm34.intervirt.api

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
    private val ioClient: ContainerIOClient
) {
    private val logger = KotlinLogging.logger {  }

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
                val output = flow.getTotalCommandStatus().message!!
                logger.debug { "Received during DNS lookup:\n$output" }
                val resolverOutput = json.decodeFromString<DnsResolverOutput>(output)
                resolverOutput.responses
                    .getOrNull(0)
                    ?.answers
                    ?.map { it.toDnsRecord() }
                    ?: emptyList()
            },
            onFailure = { emptyList() }
        )

    }
}