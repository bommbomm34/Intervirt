package io.github.bommbomm34.intervirt.api.intervirtos

import io.github.bommbomm34.intervirt.api.ContainerClientBundle
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
    ): Result<List<DnsRecord>> {
        val baseCommandList = listOf("/usr/bin/doggo", name, "--type", type, "--nameserver", nameserver, "--json")
        val commandList = if (reverse) baseCommandList + "-x" else baseCommandList
        logger.debug { "Execute command \"${commandList.joinToString(" ")}\" for DNS lookup" }
        return ioClient.exec(
            commands = commandList
        ).mapCatching { flow ->
            flow.getCommandResult()
                .asResult()
                .mapCatching { output ->
                    val resolverOutput = defaultJson.decodeFromString<DnsResolverOutput>(output)
                    resolverOutput.responses
                        .getOrNull(0)
                        ?.answers
                        ?.map { it.toDnsRecord() }
                        ?: throw IllegalStateException("DNS Resolver responded with invalid JSON schema: $output")
                }
                .getOrThrow()
        }
    }
}