package io.github.bommbomm34.intervirt.intervirtos.mail.server

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.email_address
import intervirt.ui.generated.resources.username
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.buttons.AddButton
import io.github.bommbomm34.intervirt.components.buttons.RemoveButton
import io.github.bommbomm34.intervirt.components.tables.SimpleTable
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailServerManager
import io.github.bommbomm34.intervirt.intervirtos.model.mail.MailServerUserManagerViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val headers = listOf(
    Res.string.username,
    Res.string.email_address,
)

@Composable
fun MailServerUserManager(
    mailServer: MailServerManager,
) {
    val viewModel = koinViewModel<MailServerUserManagerViewModel> { parametersOf(mailServer) }
    GeneralSpacer()
    SimpleTable(
        headers = headers.map { stringResource(it) } + "",
        content = viewModel.users.map { listOf(it.username, it.address) },
        customElements = viewModel.users.map {
            {
                RemoveButton { viewModel.removeUser(it) }
            }
        },
    )
    AlignedBox(Alignment.BottomEnd) {
        AddButton(onClick = viewModel::addUser)
    }
}