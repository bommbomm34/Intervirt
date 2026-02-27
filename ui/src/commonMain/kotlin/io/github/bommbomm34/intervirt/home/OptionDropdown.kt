package io.github.bommbomm34.intervirt.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.components.buttons.IconText
import io.github.bommbomm34.intervirt.model.home.OptionDropdownViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun OptionDropdown(
    expanded: Boolean,
    onConfChange: () -> Unit,
    onDismiss: () -> Unit,
) {
    val viewModel = koinViewModel<OptionDropdownViewModel> { parametersOf(onConfChange, onDismiss) }
    Column {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
        ) {
            // Open
            DropdownMenuItem(
                onClick = viewModel::open,
                text = {
                    IconText(
                        imageVector = Icons.Default.FileOpen,
                        text = stringResource(Res.string.open),
                    )
                },
            )
            // Save
            DropdownMenuItem(
                onClick = viewModel::save,
                text = {
                    IconText(
                        imageVector = Icons.Default.Save,
                        text = stringResource(Res.string.save),
                    )
                },
            )
            // Save As
            DropdownMenuItem(
                onClick = viewModel::saveAs,
                text = {
                    IconText(
                        imageVector = Icons.Default.SaveAs,
                        text = stringResource(Res.string.save_as),
                    )
                },
            )
            // Update
            DropdownMenuItem(
                onClick = viewModel::update,
                text = {
                    IconText(
                        imageVector = Icons.Default.Update,
                        text = stringResource(Res.string.update),
                    )
                },
            )
            // Settings
            DropdownMenuItem(
                onClick = viewModel::openSettings,
                text = {
                    IconText(
                        imageVector = Icons.Default.Settings,
                        text = stringResource(Res.string.settings),
                    )
                },
            )
            // About
            DropdownMenuItem(
                onClick = viewModel::openAbout,
                text = {
                    IconText(
                        imageVector = Icons.Default.Info,
                        text = stringResource(Res.string.about),
                    )
                },
            )
            // Help
            DropdownMenuItem(
                onClick = viewModel::openHelp,
                text = {
                    IconText(
                        imageVector = Icons.AutoMirrored.Filled.Help,
                        text = stringResource(Res.string.help),
                    )
                },
            )
        }
    }
}