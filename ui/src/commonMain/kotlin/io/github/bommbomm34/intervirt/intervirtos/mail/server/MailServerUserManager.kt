/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.intervirtos.mail.server

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.add_user
import intervirt.ui.generated.resources.delete
import intervirt.ui.generated.resources.email_address
import intervirt.ui.generated.resources.username
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.TooltipArea
import io.github.bommbomm34.intervirt.components.buttons.AddButton
import io.github.bommbomm34.intervirt.components.buttons.RemoveButton
import io.github.bommbomm34.intervirt.components.tables.SimpleTable
import io.github.bommbomm34.intervirt.core.data.MailUser
import org.jetbrains.compose.resources.stringResource

private val headers = listOf(
    Res.string.username,
    Res.string.email_address,
)

@Composable
fun MailServerUserManager(
    users: List<MailUser>,
    onAddUser: () -> Unit,
    onRemoveUser: (MailUser) -> Unit,
) {
    SimpleTable(
        headers = headers.map { stringResource(it) } + "",
        content = users.map { listOf(it.username, it.address) },
        customElements = users.map {
            {
                TooltipArea(Res.string.delete) {
                    RemoveButton { onRemoveUser(it) }
                }
            }
        },
    )
    AlignedBox(Alignment.BottomEnd) {
        TooltipArea(Res.string.add_user) {
            AddButton(onClick = onAddUser)
        }
    }
}
