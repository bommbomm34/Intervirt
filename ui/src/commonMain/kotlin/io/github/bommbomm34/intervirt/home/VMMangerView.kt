package io.github.bommbomm34.intervirt.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.reboot
import intervirt.ui.generated.resources.shutdown
import intervirt.ui.generated.resources.sync_guest
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.CenterRow
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.model.home.VMManagerViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@Composable
fun VMManagerView() {
    val viewModel = koinViewModel<VMManagerViewModel>()
    AlignedBox(Alignment.TopStart, padding = 16.dp) {
        CenterRow {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Shutdown button
                Button(onClick = viewModel::shutdown) {
                    Text(stringResource(Res.string.shutdown))
                }
                GeneralSpacer(4.dp)
                // Reboot button
                Button(onClick = viewModel::reboot) {
                    Text(stringResource(Res.string.reboot))
                }
            }
            GeneralSpacer()
            // Sync button
            IconButton(onClick = viewModel::sync) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(Res.string.sync_guest),
                )
            }
        }
    }
}