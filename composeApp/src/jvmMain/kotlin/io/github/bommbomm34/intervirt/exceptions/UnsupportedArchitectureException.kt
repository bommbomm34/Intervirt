package io.github.bommbomm34.intervirt.exceptions

class UnsupportedArchitectureException : Exception("Architecture ${System.getProperty("os.arch")} is not supported")