/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

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
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.GeneralIcon
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.buttons.SendButton
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailClientManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.mail.MailConnectionDetails
import io.github.bommbomm34.intervirt.intervirtos.mail.client.MailListView
import io.github.bommbomm34.intervirt.intervirtos.model.MailClientViewModel
import io.github.bommbomm34.intervirt.rememberManager
import io.github.bommbomm34.intervirt.rememberProxyManager
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MailClient(
    osClient: IntervirtOSClient,
) {
    val viewModel = koinViewModel<MailClientViewModel> { parametersOf(osClient) }

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

            CatchingLaunchedEffect(viewModel.client) {
                credentials = viewModel.client.loadCredentials().getOrThrow()
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