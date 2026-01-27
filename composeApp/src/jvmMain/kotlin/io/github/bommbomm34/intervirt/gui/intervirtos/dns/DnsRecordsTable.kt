package io.github.bommbomm34.intervirt.gui.intervirtos.dns

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer

@Composable
fun DnsRecordsTable(records: List<DnsRecord>){
    LazyColumn {
        items(records){
            DnsRecordItem(it)
            GeneralSpacer(2.dp)
            HorizontalDivider()
        }
    }
}