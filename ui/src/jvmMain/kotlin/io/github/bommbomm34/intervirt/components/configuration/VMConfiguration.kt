package io.github.bommbomm34.intervirt.components.configuration

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.core.data.VMConfigurationData
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.NamedCheckbox
import io.github.bommbomm34.intervirt.gui.components.textfields.IntegerTextField
import org.jetbrains.compose.resources.stringResource

@Composable
fun VMConfiguration(
    conf: VMConfigurationData,
    onConfChange: (VMConfigurationData) -> Unit,
) {
    _root_ide_package_.io.github.bommbomm34.intervirt.components.CenterColumn {
        Text(stringResource(Res.string.vm_setup_introduction))
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        _root_ide_package_.io.github.bommbomm34.intervirt.components.textfields.IntegerTextField(
            value = conf.ram,
            onValueChange = { onConfChange(conf.copy(ram = it)) },
            label = stringResource(Res.string.ram_in_mb),
        )
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        _root_ide_package_.io.github.bommbomm34.intervirt.components.textfields.IntegerTextField(
            value = conf.cpu,
            onValueChange = { onConfChange(conf.copy(cpu = it)) },
            label = stringResource(Res.string.amount_of_cpu_cores),
        )
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        _root_ide_package_.io.github.bommbomm34.intervirt.components.NamedCheckbox(
            checked = conf.kvm,
            onCheckedChange = { onConfChange(conf.copy(kvm = it)) },
            name = stringResource(Res.string.enable_kvm),
            tooltip = stringResource(Res.string.enable_kvm_tooltip),
        )
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
        DiskUrlConfiguration(conf, onConfChange)
    }
}