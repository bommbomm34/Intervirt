package io.github.bommbomm34.intervirt.gui.intervirtos.dns

import androidx.compose.runtime.Composable
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.core.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.gui.components.tables.SimpleTable
import org.jetbrains.compose.resources.stringResource

@Composable
fun DnsRecordsTable(records: List<DnsRecord>) {
    SimpleTable(
        headers = listOf(
            stringResource(Res.string.name),
            "TTL",
            stringResource(Res.string.class_name),
            stringResource(Res.string.type),
            stringResource(Res.string.data),
        ),
        content = records.map { listOf(it.name, it.ttl, it.dnsClass, it.type, it.data) },
    )
}