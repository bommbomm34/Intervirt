package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.download_file
import intervirt.ui.generated.resources.terminal
import intervirt.ui.generated.resources.upload_file
import io.github.bommbomm34.intervirt.components.GeneralIcon
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.data.ViewDevice
import io.github.bommbomm34.intervirt.model.components.IOOptionsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun IOOptions(device: ViewDevice.Computer) {
    val viewModel = koinViewModel<IOOptionsViewModel> { parametersOf(device) }
    viewModel.ioClient?.let { _ ->
        IconButton(
            onClick = viewModel::download,
        ) {
            GeneralIcon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = stringResource(Res.string.download_file),
            )
        }
        GeneralSpacer()
        IconButton(
            onClick = viewModel::upload,
        ) {
            GeneralIcon(
                imageVector = Icons.Default.FileUpload,
                contentDescription = stringResource(Res.string.upload_file),
            )
        }
        GeneralSpacer()
    }
    IconButton(
        onClick = viewModel::openShell,
    ) {
        GeneralIcon(
            imageVector = Icons.Default.Terminal,
            contentDescription = stringResource(Res.string.terminal),
        )
    }
}