package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.core.api.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailServerManager
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.intervirtos.components.NamedSystemServiceView
import io.github.bommbomm34.intervirt.gui.intervirtos.mail.server.MailServerUserManager
import io.github.bommbomm34.intervirt.rememberManager

@Composable
fun MailServer(
    osClient: IntervirtOSClient,
) {
    val mailServer = osClient.rememberManager(::MailServerManager)
    // Controls for server
    AlignedBox(Alignment.TopEnd) {
        NamedSystemServiceView(
            displayName = "SMTP",
            serviceName = "postfix",
            serviceManager = mailServer.serviceManager,
        )
        GeneralSpacer()
        NamedSystemServiceView(
            displayName = "IMAP",
            serviceName = "dovecot",
            serviceManager = mailServer.serviceManager,
        )
    }
    GeneralSpacer()
    MailServerUserManager(mailServer)
}