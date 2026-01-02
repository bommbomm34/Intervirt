package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.amount_of_cpu_cores
import intervirt.composeapp.generated.resources.arch_is_not_supported
import intervirt.composeapp.generated.resources.enable_kvm
import intervirt.composeapp.generated.resources.enable_kvm_tooltip
import intervirt.composeapp.generated.resources.ram_in_mb
import intervirt.composeapp.generated.resources.vm_setup_introduction
import io.github.bommbomm34.intervirt.data.VMConfigurationData
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.getStringArray
import org.jetbrains.compose.resources.stringResource

@Composable
fun VMConfiguration(
    conf: VMConfigurationData,
    onConfChange: (VMConfigurationData) -> Unit
) {
    CenterColumn {
        Text(stringResource(Res.string.vm_setup_introduction))
        GeneralSpacer()
        IntegerTextField(
            value = conf.ram,
            onValueChange = { onConfChange(conf.copy(ram = it)) },
            label = stringResource(Res.string.ram_in_mb)
        )
        GeneralSpacer()
        IntegerTextField(
            value = conf.cpu,
            onValueChange = { onConfChange(conf.copy(cpu = it)) },
            label = stringResource(Res.string.amount_of_cpu_cores)
        )
        GeneralSpacer()
        NamedCheckbox(
            checked = conf.kvm,
            onCheckedChange = { onConfChange(conf.copy(kvm = it)) },
            name = stringResource(Res.string.enable_kvm),
            tooltip = stringResource(Res.string.enable_kvm_tooltip)
        )
    }
}