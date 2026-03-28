/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.util.ext

import io.github.bommbomm34.intervirt.components.dialogs.ProgressDialog
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.data.syncProject
import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.bommbomm34.intervirt.core.util.Atomic
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.toViewProject
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.flow.onCompletion
import kotlinx.serialization.json.Json

suspend fun PlatformFile.writeConf(project: Project) = writeString(defaultJson.encodeToString(project))

suspend fun PlatformFile.loadConf(
    project: Atomic<Project>,
    appState: AppState,
    guestManager: GuestManager,
    onComplete: () -> Unit = {},
) {
    val fileContent = readString()
    val newConfiguration = Json.decodeFromString<Project>(fileContent)
    project.set(newConfiguration)
    val flow = guestManager.syncProject(project.get()).onCompletion { onComplete() }
    appState.openDialog {
        ProgressDialog(
            flow = flow,
            onClose = ::close,
        )
    }
    appState.statefulProject.update(newConfiguration.toViewProject())
}