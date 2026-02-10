package io.github.bommbomm34.intervirt.gui.components.configuration

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import intervirt.composeapp.generated.resources.*
import io.github.bommbomm34.intervirt.data.VMConfigurationData
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.IntegerTextField
import io.github.bommbomm34.intervirt.gui.components.NamedCheckbox
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
        GeneralSpacer()
        DiskUrlConfiguration(conf, onConfChange)
    }
}