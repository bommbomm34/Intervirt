package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.NamedCheckbox
import io.github.bommbomm34.intervirt.core.api.intervirtos.DnsResolverManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.intervirtos.dns.DnsRecordsTable
import io.github.bommbomm34.intervirt.intervirtos.model.DnsResolverViewModel
import io.github.bommbomm34.intervirt.rememberManager
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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
    val viewModel = koinViewModel<DnsResolverViewModel> { parametersOf(dnsResolver) }
    AlignedBox(Alignment.Center) {
        CenterColumn {
            OutlinedTextField(
                value = viewModel.domain,
                onValueChange = { viewModel.domain = it },
                label = { Text(stringResource(Res.string.domain)) },
            )
            GeneralSpacer()
            DropdownMenu(
                expanded = viewModel.expanded,
                onDismissRequest = { viewModel.expanded = false },
            ) {
                DNS_RECORD_TYPES.forEach { type ->
                    DropdownMenuItem(
                        onClick = { viewModel.dnsRecordType = type },
                        text = { Text(viewModel.dnsRecordType) },
                    )
                }
            }
            GeneralSpacer()
            OutlinedTextField(
                value = viewModel.dnsServer,
                onValueChange = { viewModel.dnsServer = it },
                label = { Text(stringResource(Res.string.dns_server)) },
            )
            GeneralSpacer()
            NamedCheckbox(
                checked = viewModel.reverseLookup,
                onCheckedChange = { viewModel.reverseLookup = it },
                name = stringResource(Res.string.reverse_lookup),
            )
            GeneralSpacer()
            Button(onClick = viewModel::lookup) {
                Text(stringResource(Res.string.lookup))
            }
            GeneralSpacer()
            DnsRecordsTable(viewModel.records)
        }
    }
}