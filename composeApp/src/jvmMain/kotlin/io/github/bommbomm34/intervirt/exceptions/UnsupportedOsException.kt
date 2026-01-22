package io.github.bommbomm34.intervirt.exceptions

class UnsupportedOsException : Exception("Os ${System.getProperty("os.name")} is not supported")