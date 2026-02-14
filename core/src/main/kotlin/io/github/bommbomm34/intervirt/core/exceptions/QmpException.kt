package io.github.bommbomm34.intervirt.core.exceptions

import io.github.bommbomm34.intervirt.core.data.qemu.QmpErrorBody


class QmpException(error: QmpErrorBody) : Exception(error.description)