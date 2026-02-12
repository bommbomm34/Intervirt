package io.github.bommbomm34.intervirt.api.intervirtos

import io.github.bommbomm34.intervirt.api.ContainerClientBundle
import io.github.bommbomm34.intervirt.api.ContainerIOClient
import io.github.bommbomm34.intervirt.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.data.dns.DnsResolverOutput
import io.github.bommbomm34.intervirt.data.getCommandResult
import io.github.bommbomm34.intervirt.defaultJson
import io.github.oshai.kotlinlogging.KotlinLogging

class DnsResolverManager(
    bundle: ContainerClientBundle
) {
    private val ioClient = bundle.ioClient
    private val logger = KotlinLogging.logger { }

    suspend fun lookupDns(
        name: String,
        type: String,
        nameserver: String,
        reverse: Boolean
    ): List<DnsRecord> {
        val baseCommandList = listOf("/usr/bin/doggo", name, "--type", type, "--nameserver", nameserver, "--json")
        val commandList = if (reverse) baseCommandList + "-x" else baseCommandList
        logger.debug { "Execute command \"${commandList.joinToString(" ")}\" for DNS lookup" }
        return ioClient.exec(
            commands = commandList
        ).fold(
            onSuccess = { flow ->
                val (output, statusCode) = flow.getCommandResult()
                logger.debug { "Received during DNS lookup:\n$output" }
                if (statusCode == 0) {
                    val resolverOutput = defaultJson.decodeFromString<DnsResolverOutput>(output)
                    resolverOutput.responses
                        .getOrNull(0)
                        ?.answers
                        ?.map { it.toDnsRecord() }
                        ?: emptyList()
                } else {
                    logger.error { "Unexpected status code during DNS lookup: $statusCode" }
                    emptyList()
                }
            },
            onFailure = { emptyList() }
        )
    }
}