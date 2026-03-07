/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.exceptions

class AgentTimeoutException(uuid: String) : Exception("Exceeded timeout of request $uuid")