package io.github.bommbomm34.intervirt.components.configuration

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.NamedCheckbox
import io.github.bommbomm34.intervirt.components.textfields.IntegerTextField
import io.github.bommbomm34.intervirt.core.data.AppEnv
import org.jetbrains.compose.resources.stringResource

@Composable
fun VMConfiguration(appEnv: AppEnv) {
    CenterColumn {
        Text(stringResource(Res.string.vm_setup_introduction))
        GeneralSpacer()
        IntegerTextField(
            value = appEnv.VM_RAM,
            onValueChange = { appEnv.VM_RAM = it },
            label = stringResource(Res.string.ram_in_mb),
        )
        GeneralSpacer()
        IntegerTextField(
            value = appEnv.VM_CPU,
            onValueChange = { appEnv.VM_CPU = it },
            label = stringResource(Res.string.amount_of_cpu_cores),
        )
        GeneralSpacer()
        NamedCheckbox(
            checked = appEnv.VM_ENABLE_KVM,
            onCheckedChange = { appEnv.VM_ENABLE_KVM = it },
            name = stringResource(Res.string.enable_kvm),
            tooltip = stringResource(Res.string.enable_kvm_tooltip),
        )
        GeneralSpacer()
        DiskUrlConfiguration(appEnv)
    }
}