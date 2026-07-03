package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.api.intervirtos.general.impl.emitEnd
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.impl.emitError
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.impl.emitRunning
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.dns.DnsRecordAnswer
import io.github.bommbomm34.intervirt.core.data.dns.DnsResolverOutput
import io.github.bommbomm34.intervirt.core.data.dns.DnsResponse
import io.github.bommbomm34.intervirt.core.defaultJson
import kotlinx.coroutines.flow.FlowCollector

abstract class ContainerIOClientExecInterceptor(val command: String) {
    abstract suspend fun FlowCollector<CommandStatus>.intercept(args: List<String>)

    companion object {
        val DEFAULT_VIRTUAL_INTERCEPTORS: List<ContainerIOClientExecInterceptor> = listOf(DnsResolverInterceptor)
    }
}

object DnsResolverInterceptor : ContainerIOClientExecInterceptor("/usr/bin/doggo") {
    override suspend fun FlowCollector<CommandStatus>.intercept(args: List<String>) {
        if (args.size < 6) {
            emitError("Expected at least six arguments, but got $args")
        }

        val (name, _, type) = args
        val output = DnsResolverOutput(
            responses = listOf(DnsResponse(resolve(name, type)))
        )

        emitRunning(defaultJson.encodeToString(output))
        emitEnd(0)
    }

    private fun resolve(name: String, type: String): List<DnsRecordAnswer> {
        return when (name) {
            "google.com" -> when (type) {
                "A" -> answer(name, type, "142.250.154.101")
                "AAAA" -> answer(name, type, "2a00:1450:4001:c13::8a")
                else -> emptyList()
            }
            "example.com" -> when (type) {
                "A" -> answer(name, type, "104.20.23.154")
                "AAAA" -> answer(name, type, "2606:4700:10::6814:179a")
                else -> emptyList()
            }
            else -> emptyList()
        }
    }

    private fun answer(
        name: String,
        type: String,
        address: String,
    ): List<DnsRecordAnswer> {
        return listOf(
            DnsRecordAnswer(
                name = name,
                type = type,
                dnsClass = "IN",
                ttl = "5m",
                address = address,
                status = "",
                nameserver = "1.1.1.1",
            )
        )
    }
}
