/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.configuration

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.NamedCheckbox
import io.github.bommbomm34.intervirt.components.textfields.IntegerTextField
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import org.jetbrains.compose.resources.stringResource

@Composable
fun VMConfiguration(
    appEnv: AppEnv,
    onEnvChange: (AppEnv) -> Unit,
) {
    CenterColumn {
        Text(stringResource(Res.string.vm_setup_introduction))
        GeneralSpacer()
        IntegerTextField(
            value = appEnv.vmRam,
            onValueChange = { onEnvChange(appEnv.copy(vmRam = it)) },
            label = stringResource(Res.string.ram_in_mb),
        )
        GeneralSpacer()
        IntegerTextField(
            value = appEnv.vmCpu,
            onValueChange = { onEnvChange(appEnv.copy(vmCpu = it)) },
            label = stringResource(Res.string.amount_of_cpu_cores),
        )
        GeneralSpacer()
        NamedCheckbox(
            checked = appEnv.vmEnableKvm,
            onCheckedChange = { onEnvChange(appEnv.copy(vmEnableKvm = it)) },
            name = stringResource(Res.string.enable_kvm),
            tooltip = stringResource(Res.string.enable_kvm_tooltip),
        )
        GeneralSpacer()
        DiskUrlConfiguration(appEnv, onEnvChange)
    }
}
