package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Refresh
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.refresh
import io.github.bommbomm34.intervirt.core.api.ContainerClientBundle
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.MailClientManager
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.Mail
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralIcon
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.intervirtos.mail.client.MailClientLogin
import io.github.bommbomm34.intervirt.gui.intervirtos.mail.client.MailListView
import io.github.bommbomm34.intervirt.rememberClient
import io.github.bommbomm34.intervirt.rememberLogger
import io.github.bommbomm34.intervirt.rememberProxyManager
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun MailClient(
    bundle: ContainerClientBundle
) {
    val logger = rememberLogger("MailClient")
    val appState = koinInject<AppState>()
    val appEnv = koinInject<AppEnv>()
    val deviceManager = koinInject<DeviceManager>()
    val client = bundle.rememberClient(::MailClientManager)
    val proxyClient = rememberProxyManager(appEnv, bundle)
    val mails = remember { mutableStateListOf<Mail>() }
    var proxyUrl: Address? by remember { mutableStateOf(null) }
    var initialized by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    // Get proxy
    LaunchedEffect(Unit) {
        appState.runDialogCatching {
            proxyUrl = proxyClient.getProxyUrl(deviceManager).getOrThrow()
        }
    }
    suspend fun loadMails() {
        mails.clear()
        mails.addAll(client.getMails().getOrThrow())
    }
    proxyUrl?.let { proxy ->
        if (initialized) {
            CenterColumn {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Refresh button
                    IconButton(
                        onClick = {
                            scope.launch {
                                appState.runDialogCatching {
                                    loadMails()
                                }
                            }
                        }
                    ) {
                        GeneralIcon(
                            imageVector = TablerIcons.Refresh,
                            contentDescription = stringResource(Res.string.refresh)
                        )
                    }
                }
                GeneralSpacer(2.dp)
                MailListView(client, mails) {
                    logger.debug { "Clicked on mail \"${it.subject}\"" }
                }
            }
        } else {
            appState.openDialog {
                MailClientLogin { details ->
                    appState.closeDialog()
                    scope.launch {
                        appState.runDialogCatching {
                            client.init(
                                mailConnectionDetails = details,
                                proxy = proxy
                            ).getOrThrow()
                            initialized = true
                            loadMails()
                        }
                    }
                }
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            scope.launch {
                appState.runDialogCatching {
                    client.close().getOrThrow()
                    proxyClient.close(deviceManager).getOrThrow()
                }
            }
        }
    }
}