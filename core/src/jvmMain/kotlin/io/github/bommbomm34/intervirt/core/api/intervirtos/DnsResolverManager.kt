/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.intervirtos

import arrow.core.flatMap
import arrow.core.left
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.AppResult
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.core.data.dns.DnsResolverOutput
import io.github.bommbomm34.intervirt.core.data.getCommandResult
import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.bommbomm34.intervirt.core.util.ext.getLogger


class DnsResolverManager(
    appEnv: AppEnv,
    private val ioClient: ContainerIOClient,
) {
    private val logger = appEnv.getLogger(DnsResolverManager::class)

    suspend fun lookupDns(
        name: String,
        type: String,
        nameserver: String,
        reverse: Boolean,
    ): AppResult<List<DnsRecord>> {
        val baseCommandList = listOf("/usr/bin/doggo", name, "--type", type, "--nameserver", nameserver, "--json")
        val commandList = if (reverse) baseCommandList + "-x" else baseCommandList
        logger.debug { "Execute command \"${commandList.joinToString(" ")}\" for DNS lookup" }
        return ioClient.exec(
            commands = commandList,
        ).flatMap exec@ { flow ->
            flow.getCommandResult()
                .asResult()
                .map { output ->
                    val resolverOutput = defaultJson.decodeFromString<DnsResolverOutput>(output)
                    resolverOutput.responses
                        .getOrNull(0)
                        ?.answers
                        ?.map { it.toDnsRecord() }
                        ?: return@exec Failure.IllegalState("DNS Resolver responded with invalid JSON schema: $output").left()
                }
                .onLeft { return@exec it.left() }
        }
    }
}
