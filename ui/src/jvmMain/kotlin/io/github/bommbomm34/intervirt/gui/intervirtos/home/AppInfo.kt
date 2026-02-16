package io.github.bommbomm34.intervirt.gui.intervirtos.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.TablerIcons
import compose.icons.tablericons.Browser
import compose.icons.tablericons.CloudFog
import compose.icons.tablericons.Mailbox
import compose.icons.tablericons.Terminal
import compose.icons.tablericons.Window
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.core.api.ContainerClientBundle
import io.github.bommbomm34.intervirt.gui.intervirtos.*
import org.jetbrains.compose.resources.StringResource

// More apps will be added in the future :)
val INTERVIRTOS_APPS = listOf(
    AppInfo(
        name = Res.string.browser,
        icon = TablerIcons.Browser,
        description = Res.string.browser_description,
        content = ::Browser
    ),
    AppInfo(
        name = Res.string.dns_resolver,
        icon = TablerIcons.CloudFog,
        description = Res.string.dns_resolver_description,
        content = ::DnsResolver
    ),
    AppInfo(
        name = Res.string.http_server,
        icon = TablerIcons.Window, // TODO: Change it to according icon
        description = Res.string.http_server_description,
        content = ::HttpServer
    ),
    AppInfo(
        name = Res.string.ssh_server,
        icon = TablerIcons.Window,
        description = Res.string.ssh_server_description, // TODO: Change it to according icon
        content = ::SshServer
    ),
    AppInfo(
        name = Res.string.terminal,
        icon = TablerIcons.Terminal,
        description = Res.string.terminal_description,
        content = ::Terminal
    ),
    AppInfo(
        name = Res.string.mail_client,
        icon = TablerIcons.Mailbox,
        description = Res.string.mail_client_description,
        content = ::MailClient
    )
)

data class AppInfo(
    val name: StringResource,
    val icon: ImageVector,
    val description: StringResource,
    val content: @Composable (ContainerClientBundle) -> Unit
)