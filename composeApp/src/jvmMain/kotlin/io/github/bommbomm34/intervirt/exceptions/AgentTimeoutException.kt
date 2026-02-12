package io.github.bommbomm34.intervirt.exceptions

class AgentTimeoutException(uuid: String) : Exception("Exceeded timeout of request $uuid")