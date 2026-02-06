package io.github.bommbomm34.intervirt.gui.intervirtos.dns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.class_name
import intervirt.composeapp.generated.resources.data
import intervirt.composeapp.generated.resources.name
import intervirt.composeapp.generated.resources.type
import io.github.bommbomm34.intervirt.data.AppEnv
import io.github.bommbomm34.intervirt.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.isDarkMode
import io.github.windedge.table.DataTable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun DnsRecordsTable(records: List<DnsRecord>){
    SelectionContainer {
        DataTable(
            columns = {
                headerBackground {
                    Box(Modifier.background(MaterialTheme.colorScheme.onBackground))
                }
                column { VisibleText(stringResource(Res.string.name), true) }
                column { VisibleText("TTL", true) }
                column { VisibleText(stringResource(Res.string.class_name), true) }
                column { VisibleText(stringResource(Res.string.type), true) }
                column { VisibleText(stringResource(Res.string.data), true) }
            }
        ){
            records.forEach {
                row {
                    cell { VisibleText(it.name) }
                    cell { VisibleText(it.ttl) }
                    cell { VisibleText(it.dnsClass) }
                    cell { VisibleText(it.type) }
                    cell { VisibleText(it.data) }
                }
            }
        }
    }
}

@Composable
private fun VisibleText(text: Any, bold: Boolean = false){
    val appEnv = koinInject<AppEnv>()
    Text(
        text = text.toString(),
        color = if (appEnv.isDarkMode()) Color.White else Color.Black,
        fontWeight = if (bold) FontWeight.ExtraBold else null
    )
}