/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.buttons.AddButton
import io.github.bommbomm34.intervirt.components.buttons.RemoveButton
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.intervirtos.DnsServerManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.openDialog
import io.github.bommbomm34.intervirt.intervirtos.components.DockerContainerView
import io.github.bommbomm34.intervirt.intervirtos.dns.DnsRecordsTable
import io.github.bommbomm34.intervirt.intervirtos.dns.server.AddDnsRecordView
import io.github.bommbomm34.intervirt.util.ext.initialize
import io.github.bommbomm34.intervirt.util.ext.rememberManager
import jdk.internal.net.http.common.Utils.close
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
        CatchingLaunchedEffect(dnsServer) {
            records.clear()
            records.addAll(dnsServer.listRecords())
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
                    AddDnsRecordView(::close) {
                        scope.launchDialogCatching(appState) {
                            dnsServer.addRecord(it)
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
                            scope.launchDialogCatching(appState) {
                                dnsServer.removeRecord(record)
                                records.remove(record)
                            }
                        }
                    }
                },
            )
        }
    }
}
