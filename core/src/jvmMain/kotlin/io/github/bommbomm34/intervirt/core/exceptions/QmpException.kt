/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.exceptions

import io.github.bommbomm34.intervirt.core.data.qemu.QmpErrorBody


class QmpException(error: QmpErrorBody) : Exception(error.description)