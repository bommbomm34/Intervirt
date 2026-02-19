package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.core.api.intervirtos.DnsServerManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.intervirtos.components.DockerContainerView
import io.github.bommbomm34.intervirt.gui.intervirtos.dns.DnsRecordsTable
import io.github.bommbomm34.intervirt.initialize
import io.github.bommbomm34.intervirt.rememberManager

@Composable
fun DnsServer(
    osClient: IntervirtOSClient
){
    val dnsServer = osClient.rememberManager(::DnsServerManager)
    val initialized by dnsServer.initialize()
    val records = remember { mutableStateListOf<DnsRecord>() }

    if (initialized){
        CatchingLaunchedEffect {
            records.clear()
            records.addAll(dnsServer.listRecords().getOrThrow())
        }
        AlignedBox(Alignment.TopStart){
            DockerContainerView(
                name = dnsServer.containerName,
                dockerManager = dnsServer.docker
            )
        }
        CenterColumn {
            DnsRecordsTable(records)
        }
    }
}