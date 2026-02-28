package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.refresh
import intervirt.ui.generated.resources.sure_to_delete_mail
import io.github.bommbomm34.intervirt.components.*
import io.github.bommbomm34.intervirt.components.buttons.SendButton
import io.github.bommbomm34.intervirt.components.dialogs.AcceptDialog
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailClientManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.Mail
import io.github.bommbomm34.intervirt.core.data.mail.MailConnectionDetails
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.intervirtos.mail.client.MailClientLogin
import io.github.bommbomm34.intervirt.intervirtos.mail.client.MailEditor
import io.github.bommbomm34.intervirt.intervirtos.mail.client.MailListView
import io.github.bommbomm34.intervirt.intervirtos.mail.client.MailView
import io.github.bommbomm34.intervirt.intervirtos.model.MailClientViewModel
import io.github.bommbomm34.intervirt.rememberManager
import io.github.bommbomm34.intervirt.rememberProxyManager
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MailClient(
    osClient: IntervirtOSClient,
) {
    val appEnv = koinInject<AppEnv>()
    val deviceManager = koinInject<DeviceManager>()
    val client = osClient.rememberManager(::MailClientManager)
    val proxyClient = rememberProxyManager(appEnv, deviceManager, osClient)
    val viewModel = koinViewModel<MailClientViewModel> { parametersOf(client, proxyClient) }

    viewModel.proxyUrl?.let { proxy ->
        if (viewModel.initialized) {
            // Send button
            AlignedBox(Alignment.BottomEnd) {
                SendButton { viewModel.openMailEditor() }
            }
            CenterColumn {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    // Refresh button
                    IconButton(
                        onClick = viewModel::loadMails,
                    ) {
                        GeneralIcon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(Res.string.refresh),
                        )
                    }
                }
                GeneralSpacer(2.dp)
                MailListView(viewModel.mails, viewModel::clickMail)
            }
        } else {
            var credentials: MailConnectionDetails? by remember { mutableStateOf(null) }

            LaunchedEffect(Unit) {
                credentials = client.loadCredentials()
            }

            credentials?.let { creds ->
                if (creds.smtpAddress != Address.EXAMPLE
                    && creds.imapAddress != Address.EXAMPLE
                    && creds.username.isNotEmpty()
                    && creds.password.isNotEmpty()
                ) {
                    // Implicit login
                    viewModel.login(creds, true, proxy)
                } else {
                    viewModel.newLogin(creds, proxy)
                }
            }
        }
    }
}