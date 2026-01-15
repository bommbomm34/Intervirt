package io.github.bommbomm34.intervirt.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.mikepenz.aboutlibraries.ui.compose.LibraryColors
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import intervirt.composeapp.generated.resources.Res
import io.github.bommbomm34.intervirt.currentScreenIndex
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.buttons.BackButton

@Composable
fun About(){
    val libraries by produceLibraries {
        Res.readBytes("files/libraries.json").decodeToString()
    }
    Column {
        BackButton { currentScreenIndex = 1 }
        GeneralSpacer()
        LibrariesContainer(libraries)
    }
}