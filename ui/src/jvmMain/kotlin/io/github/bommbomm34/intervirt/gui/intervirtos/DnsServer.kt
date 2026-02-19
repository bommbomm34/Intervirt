package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.core.api.intervirtos.DnsServerManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.buttons.AddButton
import io.github.bommbomm34.intervirt.gui.components.buttons.RemoveButton
import io.github.bommbomm34.intervirt.gui.components.launchDialogCatching
import io.github.bommbomm34.intervirt.gui.intervirtos.components.DockerContainerView
import io.github.bommbomm34.intervirt.gui.intervirtos.dns.DnsRecordsTable
import io.github.bommbomm34.intervirt.gui.intervirtos.dns.server.AddDnsRecordView
import io.github.bommbomm34.intervirt.initialize
import io.github.bommbomm34.intervirt.rememberManager
import org.koin.compose.koinInject

@Composable
fun DnsServer(
    osClient: IntervirtOSClient,
) {
    val appState = koinInject<AppState>()
    val scope = rememberCoroutineScope()
    val dnsServer = osClient.rememberManager(::DnsServerManager)
    val initialized by dnsServer.initialize()
    val records = remember { mutableStateListOf<DnsRecord>() }

    if (initialized) {
        CatchingLaunchedEffect {
            records.clear()
            records.addAll(dnsServer.listRecords().getOrThrow())
        }
        // Start/Stop
        AlignedBox(Alignment.TopStart) {
            DockerContainerView(
                name = dnsServer.containerName,
                dockerManager = dnsServer.docker,
            )
        }
        // Add
        AlignedBox(Alignment.BottomEnd) {
            AddButton {
                appState.openDialog {
                    AddDnsRecordView(appState::closeDialog) {
                        scope.launchDialogCatching(appState) {
                            dnsServer.addRecord(it).getOrThrow()
                            records.add(it)
                        }
                    }
                }
            }
        }
        // Records
        CenterColumn {
            DnsRecordsTable(
                records = records,
                customElements = records.map {
                    { record ->
                        RemoveButton {
                            scope.launchDialogCatching(appState){
                                dnsServer.removeRecord(record).getOrThrow()
                                records.remove(record)
                            }
                        }
                    }
                }
            )
        }
    }
}