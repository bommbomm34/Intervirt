package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailServerManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.intervirtos.components.DockerContainerView
import io.github.bommbomm34.intervirt.gui.intervirtos.mail.server.MailServerUserManager
import io.github.bommbomm34.intervirt.initialize
import io.github.bommbomm34.intervirt.rememberManager
import kotlinx.coroutines.delay

@Composable
fun MailServer(
    osClient: IntervirtOSClient,
) {
    val mailServer = osClient.rememberManager(::MailServerManager)
    var initialized by mailServer.initialize()

    if (initialized) {
        // Controls for server
        AlignedBox(Alignment.TopEnd) {
            DockerContainerView(
                name = mailServer.containerName,
                dockerManager = mailServer.docker,
            )
        }
        GeneralSpacer()
        MailServerUserManager(mailServer)
    }
}