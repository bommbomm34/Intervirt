package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.intervirtos.home.AppInfo
import io.github.bommbomm34.intervirt.gui.intervirtos.home.AppItem
import io.github.bommbomm34.intervirt.gui.intervirtos.home.INTERVIRTOS_APPS
import org.koin.compose.koinInject

@Composable
fun Home(
    onAppChange: (io.github.bommbomm34.intervirt.intervirtos.home.AppInfo) -> Unit,
) {
    val appEnv = koinInject<AppEnv>()
    _root_ide_package_.io.github.bommbomm34.intervirt.components.CenterColumn {
        Text(
            text = "IntervirtOS",
            fontSize = appEnv.TITLE_FONT_SIZE.sp,
        )
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.Center) {
            LazyVerticalGrid(
                columns = GridCells.FixedSize(appEnv.OS_ICON_SIZE.dp * 1.5f),
            ) {
                items(_root_ide_package_.io.github.bommbomm34.intervirt.intervirtos.home.INTERVIRTOS_APPS) { appInfo ->
                    _root_ide_package_.io.github.bommbomm34.intervirt.intervirtos.home.AppItem(
                        appInfo = appInfo,
                        onClick = onAppChange,
                    )
                }
            }
        }
    }
}