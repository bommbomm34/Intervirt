package io.github.bommbomm34.intervirt.gui.intervirtos.dns

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import io.github.bommbomm34.intervirt.data.dns.DnsRecord

@Composable
fun DnsRecordItem(record: DnsRecord){
    // TODO: Display it more user-friendly
    Text("${record.name} ${record.ttl} ${record.dnsClass} ${record.type} ${record.data}")
}