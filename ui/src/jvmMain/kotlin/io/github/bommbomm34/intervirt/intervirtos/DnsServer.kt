package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.intervirtos.DnsServerManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.buttons.AddButton
import io.github.bommbomm34.intervirt.gui.components.buttons.RemoveButton
import io.github.bommbomm34.intervirt.gui.components.dialogs.launchDialogCatching
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
        _root_ide_package_.io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect {
            records.clear()
            records.addAll(dnsServer.listRecords().getOrThrow())
        }
        // Start/Stop
        _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.TopStart) {
            _root_ide_package_.io.github.bommbomm34.intervirt.intervirtos.components.DockerContainerView(
                name = dnsServer.containerName,
                dockerManager = dnsServer.docker,
            )
        }
        // Add
        _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.BottomEnd) {
            _root_ide_package_.io.github.bommbomm34.intervirt.components.buttons.AddButton {
                appState.openDialog {
                    _root_ide_package_.io.github.bommbomm34.intervirt.intervirtos.dns.server.AddDnsRecordView(::close) {
                        scope.launchDialogCatching(appState) {
                            dnsServer.addRecord(it).getOrThrow()
                            records.add(it)
                        }
                    }
                }
            }
        }
        // Records
        _root_ide_package_.io.github.bommbomm34.intervirt.components.CenterColumn {
            _root_ide_package_.io.github.bommbomm34.intervirt.intervirtos.dns.DnsRecordsTable(
                records = records,
                customElements = records.map {
                    { record ->
                        _root_ide_package_.io.github.bommbomm34.intervirt.components.buttons.RemoveButton {
                            scope.launchDialogCatching(appState) {
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