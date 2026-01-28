package io.github.bommbomm34.intervirt.gui.intervirtos.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.TablerIcons
import compose.icons.tablericons.Browser
import compose.icons.tablericons.CloudFog
import compose.icons.tablericons.Terminal
import compose.icons.tablericons.Window
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.browser
import intervirt.composeapp.generated.resources.browser_description
import intervirt.composeapp.generated.resources.dns_resolver
import intervirt.composeapp.generated.resources.dns_resolver_description
import intervirt.composeapp.generated.resources.http_server
import intervirt.composeapp.generated.resources.http_server_description
import intervirt.composeapp.generated.resources.ssh_server
import intervirt.composeapp.generated.resources.ssh_server_description
import intervirt.composeapp.generated.resources.terminal
import intervirt.composeapp.generated.resources.terminal_description
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.intervirtos.Browser
import io.github.bommbomm34.intervirt.gui.intervirtos.DnsResolver
import io.github.bommbomm34.intervirt.gui.intervirtos.HttpServer
import io.github.bommbomm34.intervirt.gui.intervirtos.SshServer
import io.github.bommbomm34.intervirt.gui.intervirtos.Terminal
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
    )
)

data class AppInfo(
    val name: StringResource,
    val icon: ImageVector,
    val description: StringResource,
    val content: @Composable (ViewDevice.Computer) -> Unit
)