package io.github.bommbomm34.intervirt.intervirtos.dns.server

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.add
import intervirt.ui.generated.resources.data
import intervirt.ui.generated.resources.name
import intervirt.ui.generated.resources.type
import io.github.bommbomm34.intervirt.core.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.SelectionDropdown
import io.github.bommbomm34.intervirt.gui.components.buttons.CloseButton
import io.github.bommbomm34.intervirt.gui.components.textfields.IntegerTextField
import io.github.bommbomm34.intervirt.gui.components.textfields.ReadOnlyTextField
import io.github.bommbomm34.intervirt.gui.components.textfields.SimpleTextField
import org.jetbrains.compose.resources.stringResource

private val DNS_CLASSES = listOf(
    "A",
    "AAAA",
    "CNAME",
    "MX",
    "NS",
    "SOA",
    "PTR",
    "TXT",
    "SRV",
    "CAA",
    "NAPTR",
    "DNSKEY",
    "DS",
    "RRSIG",
    "NSEC",
    "NSEC3",
    "TLSA",
    "SPF",
    "LOC",
    "HINFO",
    "RP",
    "SSHFP",
    "SVCB",
    "HTTPS",
)

@Composable
fun AddDnsRecordView(
    onCancel: () -> Unit = {},
    onAdd: (DnsRecord) -> Unit,
) {
    var name by remember { mutableStateOf("example.com.") }
    var ttl by remember { mutableStateOf(3600) }
    var dnsClass by remember { mutableStateOf("A") }
    var data by remember { mutableStateOf("104.18.27.120") }
    _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.TopStart) {
        _root_ide_package_.io.github.bommbomm34.intervirt.components.buttons.CloseButton(onCancel)
    }
    _root_ide_package_.io.github.bommbomm34.intervirt.components.CenterColumn {
        _root_ide_package_.io.github.bommbomm34.intervirt.components.textfields.SimpleTextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(Res.string.name),
        )
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        _root_ide_package_.io.github.bommbomm34.intervirt.components.textfields.IntegerTextField(
            value = ttl,
            onValueChange = { ttl = it },
            label = "TTL",
        )
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        _root_ide_package_.io.github.bommbomm34.intervirt.components.SelectionDropdown(
            options = DNS_CLASSES,
            selected = dnsClass,
            onSelect = { dnsClass = it },
        )
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        _root_ide_package_.io.github.bommbomm34.intervirt.components.textfields.ReadOnlyTextField(
            value = "IN",
            label = stringResource(Res.string.type),
        )
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        _root_ide_package_.io.github.bommbomm34.intervirt.components.textfields.SimpleTextField(
            value = data,
            onValueChange = { data = it },
            label = stringResource(Res.string.data),
        )
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer(16.dp)
        Button(
            onClick = {
                onAdd(
                    DnsRecord(
                        name = name,
                        ttl = ttl,
                        dnsClass = dnsClass,
                        type = "IN",
                        data = data,
                    ),
                )
            },
        ) {
            Text(stringResource(Res.string.add))
        }
    }
}