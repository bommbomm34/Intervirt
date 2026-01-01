package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.data.VMConfigurationData

@Composable
fun VMConfiguration(
    conf: VMConfigurationData,
    onConfChange: (VMConfigurationData) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IntegerTextField(
                value = conf.ram,
                onValueChange = { onConfChange(conf.copy(ram = it)) },
                label = "RAM in MB"
            )
            GeneralSpacer()
            IntegerTextField(
                value = conf.cpu,
                onValueChange = { onConfChange(conf.copy(cpu = it)) },
                label = "Amount of CPU cores"
            )
            GeneralSpacer()
            NamedCheckbox(
                checked = conf.kvm,
                onCheckedChange = { onConfChange(conf.copy(kvm = it)) },
                name = "Enable KVM (requires Linux and a KVM group membership"
            )
        }
    }
}