package io.github.bommbomm34.intervirt

import java.util.prefs.Preferences

class Preferences {
    // The Preferences API is a standard way to handle user settings in JVM applications.
    private val preferences: Preferences by lazy {
        // We use a node specific to our application package.
        Preferences.userRoot().node(this::class.java.canonicalName)
    }

    fun saveString(key: String, value: String) {
        // Put the string value into the preferences' node.
        preferences.put(key, value)
        preferences.sync()
        preferences.flush()
    }

    fun loadString(key: String): String? {
        // Retrieve the string value, returning null if the key does not exist.
        return preferences.get(key, null)
    }

    fun clearStrings() {
        preferences.clear()
        preferences.flush()
    }
}