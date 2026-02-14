package io.github.bommbomm34.intervirt.core.api.intervirtos

import io.github.bommbomm34.intervirt.core.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.core.data.dns.DnsResolverOutput
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.oshai.kotlinlogging.KotlinLogging

class DnsResolverManager(
    bundle: io.github.bommbomm34.intervirt.core.api.ContainerClientBundle
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