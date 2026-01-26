package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.api.Preferences
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.imagepicker.ImageItem
import io.github.bommbomm34.intervirt.gui.intervirtos.home.AppInfo
import io.github.bommbomm34.intervirt.gui.intervirtos.home.AppItem
import io.github.bommbomm34.intervirt.gui.intervirtos.home.INTERVIRTOS_APPS
import org.koin.compose.koinInject

@Composable
fun Home(
    onAppChange: (AppInfo) -> Unit
){
    val preferences = koinInject<Preferences>()
    CenterColumn {
        Text(
            text = "IntervirtOS",
            fontSize = preferences.TITLE_FONT_SIZE
        )
        GeneralSpacer()
        AlignedBox(Alignment.Center){
            LazyVerticalGrid(
                columns = GridCells.FixedSize(preferences.OS_ICON_SIZE * 1.2f)
            ){
                items(INTERVIRTOS_APPS){ appInfo ->
                    AppItem(
                        appInfo = appInfo,
                        onClick = onAppChange
                    )
                }
            }
        }
    }
}