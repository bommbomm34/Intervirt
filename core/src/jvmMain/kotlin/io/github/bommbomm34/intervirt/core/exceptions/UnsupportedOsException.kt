package io.github.bommbomm34.intervirt.core.exceptions

class UnsupportedOsException : Exception("OS ${System.getProperty("os.name")} is not supported")