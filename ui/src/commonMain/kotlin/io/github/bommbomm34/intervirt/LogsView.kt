package io.github.bommbomm34.intervirt

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import org.koin.compose.koinInject

@Composable
fun LogsView(logs: List<String>) {
    val appEnv = koinInject<AppEnv>()
    CenterColumn {
        Text(
            text = "Logs",
            fontSize = 64.sp,
        )
        GeneralSpacer()
        SelectionContainer {
            LazyColumn {
                items(logs) {
                    Text(
                        text = it,
                        color = Color.Gray,
                        fontSize = appEnv.TOOLTIP_FONT_SIZE.sp,
                    )
                    HorizontalDivider()
                }
            }
        }
    }

}