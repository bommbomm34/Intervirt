package io.github.bommbomm34.intervirt.core.exceptions

class UnsupportedArchitectureException : Exception("Architecture ${System.getProperty("os.arch")} is not supported")