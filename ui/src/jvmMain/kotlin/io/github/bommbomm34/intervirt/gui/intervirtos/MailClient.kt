package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import compose.icons.TablerIcons
import compose.icons.tablericons.Refresh
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.refresh
import io.github.bommbomm34.intervirt.core.api.ContainerClientBundle
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailClientManager
import io.github.bommbomm34.intervirt.core.data.Mail
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralIcon
import io.github.bommbomm34.intervirt.gui.intervirtos.mail.client.MailListView
import io.github.bommbomm34.intervirt.rememberClient
import io.github.bommbomm34.intervirt.rememberLogger
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun MailClient(
    bundle: ContainerClientBundle
){
    val logger = rememberLogger("MailClient")
    val appState = koinInject<AppState>()
    val client = bundle.rememberClient(::MailClientManager)
    val mails = remember { mutableStateListOf<Mail>() }
    val scope = rememberCoroutineScope()
    AlignedBox(Alignment.TopEnd){
        // Refresh button
        IconButton(
            onClick = {
                scope.launch {
                    appState.runDialogCatching {
                        mails.clear()
                        mails.addAll(client.getMails().getOrThrow())
                    }
                }
            }
        ){
            GeneralIcon(
                imageVector = TablerIcons.Refresh,
                contentDescription = stringResource(Res.string.refresh)
            )
        }
    }
    CenterColumn {
        MailListView(client, mails){
            logger.debug { "Clicked on mail \"${it.subject}\"" }
        }
    }
}