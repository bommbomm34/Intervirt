package io.github.bommbomm34.intervirt.core.exceptions

class AgentTimeoutException(uuid: String) : Exception("Exceeded timeout of request $uuid")