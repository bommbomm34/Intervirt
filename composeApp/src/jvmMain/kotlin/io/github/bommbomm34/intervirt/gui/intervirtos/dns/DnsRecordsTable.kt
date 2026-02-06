package io.github.bommbomm34.intervirt.gui.intervirtos.dns

import androidx.compose.runtime.Composable
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.class_name
import intervirt.composeapp.generated.resources.data
import intervirt.composeapp.generated.resources.name
import intervirt.composeapp.generated.resources.type
import io.github.bommbomm34.intervirt.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.gui.components.SimpleTable
import org.jetbrains.compose.resources.stringResource

@Composable
fun DnsRecordsTable(records: List<DnsRecord>){
    SimpleTable(
        headers = listOf(
            stringResource(Res.string.name),
            "TTL",
            stringResource(Res.string.class_name),
            stringResource(Res.string.type),
            stringResource(Res.string.data)
        ),
        content = records.map { listOf(it.name, it.ttl, it.dnsClass, it.type, it.data) }
    )
}