package io.github.bommbomm34.intervirt.gui.home

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.update
import io.github.bommbomm34.intervirt.core.api.Downloader
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.readablePercentage
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.gui.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.NamedCheckbox
import io.github.bommbomm34.intervirt.gui.components.launchDialogCatching
import io.github.bommbomm34.intervirt.rememberLogger
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun Updater(onClose: () -> Unit) {
    val logger = rememberLogger("Updater")
    val downloader = koinInject<Downloader>()
    val appState = koinInject<AppState>()
    val scope = rememberCoroutineScope()
    val updates = remember { mutableStateListOf<Downloader.Component>() }
    val applyUpdates = remember { mutableStateListOf<Downloader.Component>() }
    CatchingLaunchedEffect {
        updates.clear()
        updates.addAll(downloader.checkUpdates().getOrThrow())
    }
    CenterColumn {
        updates.forEach { component ->
            NamedCheckbox(
                checked = applyUpdates.contains(component),
                onCheckedChange = {
                    if (it) applyUpdates.add(component) else applyUpdates.remove(component)
                },
                name = component.readableName,
            )
            GeneralSpacer()
        }
    }
    GeneralSpacer()
    // Update button
    Button(
        onClick = {
            scope.launchDialogCatching(appState){
                // TODO: Show progress in GUI and add onClose
                downloader.upgrade(applyUpdates).collect {
                    val output = when (it){
                        is ResultProgress.Message<String> -> "Message ${it.message} with ${it.percentage.readablePercentage()}"
                        is ResultProgress.Proceed<String> -> "Proceed ${it.percentage.readablePercentage()}"
                        is ResultProgress.Result<String> -> "Finished with: ${it.result}"
                    }
                    logger.debug { output }
                }
            }
        }
    ){
        Text(stringResource(Res.string.update))
    }
}