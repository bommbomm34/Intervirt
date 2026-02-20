package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailServerManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
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
        _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.TopEnd) {
            _root_ide_package_.io.github.bommbomm34.intervirt.intervirtos.components.DockerContainerView(
                name = mailServer.containerName,
                dockerManager = mailServer.docker,
            )
        }
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        _root_ide_package_.io.github.bommbomm34.intervirt.components.CenterColumn(
            modifier = Modifier
                .padding(top = 50.dp)
        ) {
            _root_ide_package_.io.github.bommbomm34.intervirt.intervirtos.mail.server.MailServerUserManager(mailServer)
        }
    }
}