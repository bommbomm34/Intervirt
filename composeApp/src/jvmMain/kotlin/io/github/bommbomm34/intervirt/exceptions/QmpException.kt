package io.github.bommbomm34.intervirt.exceptions

import io.github.bommbomm34.intervirt.data.qemu.QmpErrorBody

class QmpException(error: QmpErrorBody) : Exception(error.description)