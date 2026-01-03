package io.github.bommbomm34.intervirt.exceptions

class UnsupportedOSException : Exception("OS ${System.getProperty("os.name")} is not supported")