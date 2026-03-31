/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.exceptions

class UndefinedException(
    error: String,
    uuid: String? = null,
) : AgentException(error, uuid)