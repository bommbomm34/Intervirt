package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.composeapp.generated.resources.*
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.api.Executor
import io.github.bommbomm34.intervirt.api.Preferences
import io.github.bommbomm34.intervirt.api.getTotalCommandStatus
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.data.dns.DnsResolverOutput
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.NamedCheckbox
import io.github.bommbomm34.intervirt.gui.components.buttons.CloseButton
import io.github.bommbomm34.intervirt.gui.intervirtos.dns.DnsRecordsTable
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import java.io.File

val DNS_RECORD_TYPES = listOf(
    "A",
    "AAAA",
    "CNAME",
    "MX",
    "TXT",
    "NS",
    "SOA",
    "SRV",
    "PTR"
)

private val json = Json {
    ignoreUnknownKeys = true
}

@Composable
fun DnsResolver(
    computer: ViewDevice.Computer
) {
    val preferences = koinInject<Preferences>()
    val deviceManager = koinInject<DeviceManager>()
    val logger = KotlinLogging.logger {  }
    var domain by remember { mutableStateOf("perhof.org") }
    var expanded by remember { mutableStateOf(false) }
    var dnsRecordType by remember { mutableStateOf(DNS_RECORD_TYPES[0]) }
    var dnsServer by remember { mutableStateOf(preferences.DEFAULT_DNS_SERVER) }
    var reverseLookup by remember { mutableStateOf(false) }
    val records = mutableStateListOf<DnsRecord>()
    val scope = rememberCoroutineScope { Dispatchers.IO }
    AlignedBox(Alignment.Center) {
        CenterColumn {
            OutlinedTextField(
                value = domain,
                onValueChange = { domain = it },
                label = { Text(stringResource(Res.string.domain)) }
            )
            GeneralSpacer()
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DNS_RECORD_TYPES.forEach { type ->
                    DropdownMenuItem(
                        onClick = { dnsRecordType = type }
                    ) {
                        Text(dnsRecordType)
                    }
                }
            }
            GeneralSpacer()
            OutlinedTextField(
                value = dnsServer,
                onValueChange = { dnsServer = it },
                label = { Text(stringResource(Res.string.dns_server)) }
            )
            GeneralSpacer()
            NamedCheckbox(
                checked = reverseLookup,
                onCheckedChange = { reverseLookup = it },
                name = stringResource(Res.string.reverse_lookup)
            )
            GeneralSpacer()
            Button(
                onClick = {
                    records.clear()
                    scope.launch {
                        deviceManager.lookupDns(
                            logger = logger,
                            device = computer.device,
                            name = domain,
                            type = dnsRecordType,
                            nameserver = dnsServer,
                            reverse = reverseLookup
                        )
                    }
                }
            ) {
                Text(stringResource(Res.string.lookup))
            }
            GeneralSpacer()
            DnsRecordsTable(records)
        }
    }
}

private suspend fun DeviceManager.lookupDns(
    logger: KLogger,
    device: Device.Computer,
    name: String,
    type: String,
    nameserver: String,
    reverse: Boolean
): List<DnsRecord> {
    val command = "doggo $name --type $type --nameserver $nameserver --json" + (if (reverse) "-x" else "")
    val output = StringBuilder()
    logger.debug { "Execute command \"$command\" for DNS lookup" }
    runCommand(
        computer = device,
        command = command
    ).collect { progress -> progress.message?.let { output.append(it) } }
    logger.debug { "Received during DNS lookup:\n$output" }
    val resolverOutput = json.decodeFromString<DnsResolverOutput>(output.toString())
    return resolverOutput.responses
        .getOrNull(0)
        ?.answers
        ?.map { it.toDnsRecord() }
        ?: emptyList()
}