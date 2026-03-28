/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data.mail

import io.github.bommbomm34.intervirt.core.data.Address

data class MailConnectionDetails(
    val smtpAddress: Address = Address.EXAMPLE,
    val smtpSafety: MailConnectionSafety = MailConnectionSafety.SECURE,
    val imapAddress: Address = Address.EXAMPLE,
    val imapSafety: MailConnectionSafety = MailConnectionSafety.SECURE,
    val username: String = "",
    val password: String = "",
)


