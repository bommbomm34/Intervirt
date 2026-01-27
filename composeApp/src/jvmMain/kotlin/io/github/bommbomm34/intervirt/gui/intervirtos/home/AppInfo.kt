package io.github.bommbomm34.intervirt.gui.intervirtos.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.TablerIcons
import compose.icons.tablericons.Browser
import compose.icons.tablericons.CloudFog
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.browser
import intervirt.composeapp.generated.resources.browser_description
import intervirt.composeapp.generated.resources.dns_resolver
import intervirt.composeapp.generated.resources.dns_resolver_description
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.intervirtos.Browser
import io.github.bommbomm34.intervirt.gui.intervirtos.DnsResolver
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
    )
)

data class AppInfo(
    val name: StringResource,
    val icon: ImageVector,
    val description: StringResource,
    val content: @Composable ((ViewDevice.Computer, (() -> Unit)) -> Unit)
)