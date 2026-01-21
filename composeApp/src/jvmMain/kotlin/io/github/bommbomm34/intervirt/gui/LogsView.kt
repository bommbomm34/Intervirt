package io.github.bommbomm34.intervirt.gui

import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.bommbomm34.intervirt.data.Preferences
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import org.koin.compose.koinInject

@Composable
fun LogsView(logs: List<String>){
    val preferences = koinInject<Preferences>()
    CenterColumn {
        Text(
            text = "Logs",
            fontSize = 64.sp
        )
        GeneralSpacer()
        SelectionContainer {
            LazyColumn() {
                items(logs){
                    Text(
                        text = it,
                        color = Color.Gray,
                        fontSize = preferences.TOOLTIP_FONT_SIZE
                    )
                    HorizontalDivider()
                }
            }
        }
    }

}