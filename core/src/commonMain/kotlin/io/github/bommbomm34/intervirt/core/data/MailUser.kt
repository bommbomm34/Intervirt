/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

data class MailUser(
    val username: String,
    val address: String,
) {
    companion object {
        val UNDEFINED = MailUser("undefined", "undefined@undefined")
    }

    override fun toString() = "$username <$address>"
}
