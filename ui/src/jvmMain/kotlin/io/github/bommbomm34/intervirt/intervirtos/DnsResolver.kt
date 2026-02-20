package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.api.intervirtos.DnsResolverManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.NamedCheckbox
import io.github.bommbomm34.intervirt.gui.intervirtos.dns.DnsRecordsTable
import io.github.bommbomm34.intervirt.rememberManager
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

val DNS_RECORD_TYPES = listOf(
    "A",
    "AAAA",
    "CNAME",
    "MX",
    "TXT",
    "NS",
    "SOA",
    "SRV",
    "PTR",
)

@Composable
fun DnsResolver(
    osClient: IntervirtOSClient,
) {
    val dnsResolver = osClient.rememberManager(::DnsResolverManager)
    val appEnv = koinInject<AppEnv>()
    val appState = koinInject<AppState>()
    var domain by remember { mutableStateOf("perhof.org") }
    var expanded by remember { mutableStateOf(false) }
    var dnsRecordType by remember { mutableStateOf(DNS_RECORD_TYPES[0]) }
    var dnsServer by remember { mutableStateOf(appEnv.DEFAULT_DNS_SERVER) }
    var reverseLookup by remember { mutableStateOf(false) }
    val records = remember { mutableStateListOf<DnsRecord>() }
    val scope = rememberCoroutineScope()
    _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.Center) {
        _root_ide_package_.io.github.bommbomm34.intervirt.components.CenterColumn {
            OutlinedTextField(
                value = domain,
                onValueChange = { domain = it },
                label = { Text(stringResource(Res.string.domain)) },
            )
            _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DNS_RECORD_TYPES.forEach { type ->
                    DropdownMenuItem(
                        onClick = { dnsRecordType = type },
                    ) {
                        Text(dnsRecordType)
                    }
                }
            }
            _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
            OutlinedTextField(
                value = dnsServer,
                onValueChange = { dnsServer = it },
                label = { Text(stringResource(Res.string.dns_server)) },
            )
            _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
            _root_ide_package_.io.github.bommbomm34.intervirt.components.NamedCheckbox(
                checked = reverseLookup,
                onCheckedChange = { reverseLookup = it },
                name = stringResource(Res.string.reverse_lookup),
            )
            _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
            Button(
                onClick = {
                    records.clear()
                    scope.launch {
                        appState.runDialogCatching {
                            records.addAll(
                                dnsResolver.lookupDns(
                                    name = domain,
                                    type = dnsRecordType,
                                    nameserver = dnsServer,
                                    reverse = reverseLookup,
                                ).getOrThrow(),
                            )
                        }
                    }
                },
            ) {
                Text(stringResource(Res.string.lookup))
            }
            _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
            _root_ide_package_.io.github.bommbomm34.intervirt.intervirtos.dns.DnsRecordsTable(records)
        }
    }
}