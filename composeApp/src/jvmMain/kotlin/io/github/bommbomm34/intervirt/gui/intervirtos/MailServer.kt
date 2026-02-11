package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.api.IntervirtOSClient
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.intervirtos.components.NamedSystemServiceView
import io.github.bommbomm34.intervirt.gui.intervirtos.mail.server.MailServerUserManager

@Composable
fun MailServer(
    osClient: IntervirtOSClient
){
    // Controls for server
    AlignedBox(Alignment.TopEnd){
        NamedSystemServiceView(
            displayName = "SMTP",
            serviceName = "postfix",
            serviceManager = osClient.serviceManager
        )
        GeneralSpacer()
        NamedSystemServiceView(
            displayName = "IMAP",
            serviceName = "dovecot",
            serviceManager = osClient.serviceManager
        )
    }
    GeneralSpacer()
    MailServerUserManager(osClient)
}