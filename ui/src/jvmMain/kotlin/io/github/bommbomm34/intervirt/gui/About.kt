package io.github.bommbomm34.intervirt.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import intervirt.ui.generated.resources.Res
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.buttons.BackButton
import org.koin.compose.koinInject

@Composable
fun About() {
    val appState = koinInject<AppState>()
    val libraries by produceLibraries {
        Res.readBytes("files/libraries.json").decodeToString()
    }
    Column {
        BackButton { appState.currentScreenIndex = 1 }
        GeneralSpacer()
        LibrariesContainer(libraries)
    }
}