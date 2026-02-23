package io.github.bommbomm34.intervirt.intervirtos.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.components.GeneralIcon
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import org.jetbrains.compose.resources.stringResource


@Composable
fun AppItem(
    appInfo: AppInfo,
    onClick: (AppInfo) -> Unit,
) {
    Card(
        modifier = Modifier.padding(16.dp),
        onClick = { onClick(appInfo) },
    ) {
        Column(Modifier.padding(16.dp)) {
            GeneralIcon(
                imageVector = appInfo.icon,
                contentDescription = stringResource(appInfo.description),
            )
            GeneralSpacer(2.dp)
            Text(stringResource(appInfo.name))
        }
    }
}