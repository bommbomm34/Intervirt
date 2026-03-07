/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.intervirtos.components.DockerContainerView
import io.github.bommbomm34.intervirt.intervirtos.mail.server.MailServerUserManager
import io.github.bommbomm34.intervirt.intervirtos.model.MailServerViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MailServer(
    osClient: IntervirtOSClient,
) {
    val viewModel = koinViewModel<MailServerViewModel> { parametersOf(osClient) }

    if (viewModel.initialized) {
        // Controls for server
        AlignedBox(Alignment.TopEnd) {
            DockerContainerView(
                name = viewModel.mailServer.containerName,
                dockerManager = viewModel.mailServer.docker,
            )
        }
        GeneralSpacer()
        CenterColumn(
            modifier = Modifier
                .padding(top = 50.dp),
        ) {
            MailServerUserManager(
                users = viewModel.users,
                onAddUser = viewModel::addUser,
                onRemoveUser = viewModel::removeUser,
            )
        }
    }
}