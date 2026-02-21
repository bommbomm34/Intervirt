package io.github.bommbomm34.intervirt.intervirtos.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Web
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.intervirtos.*
import org.jetbrains.compose.resources.StringResource

// More apps will be added in the future :)
val INTERVIRTOS_APPS = listOf(
    AppInfo(
        name = Res.string.browser,
        icon = Icons.Default.Web,
        description = Res.string.browser_description,
        content = ::Browser,
    ),
    AppInfo(
        name = Res.string.dns_resolver,
        icon = Icons.Default.Search,
        description = Res.string.dns_resolver_description,
        content = ::DnsResolver,
    ),
    AppInfo(
        name = Res.string.http_server,
        icon = Icons.Default.Http,
        description = Res.string.http_server_description,
        content = ::HttpServer,
    ),
    AppInfo(
        name = Res.string.ssh_server,
        icon = Icons.AutoMirrored.Filled.ScreenShare,
        description = Res.string.ssh_server_description,
        content = ::SshServer,
    ),
    AppInfo(
        name = Res.string.terminal,
        icon = Icons.Default.Terminal,
        description = Res.string.terminal_description,
        content = ::Terminal,
    ),
    AppInfo(
        name = Res.string.mail_client,
        icon = Icons.Default.Mail,
        description = Res.string.mail_client_description,
        content = ::MailClient,
    ),
    AppInfo(
        name = Res.string.mail_server,
        icon = Icons.Default.AllInbox,
        description = Res.string.mail_server_description,
        content = ::MailServer,
    ),
    AppInfo(
        name = Res.string.dns_server,
        icon = Icons.Default.Dns,
        description = Res.string.dns_server_description,
        content = ::DnsServer,
    ),
)

data class AppInfo(
    val name: StringResource,
    val icon: ImageVector,
    val description: StringResource,
    val content: @Composable (IntervirtOSClient) -> Unit,
)