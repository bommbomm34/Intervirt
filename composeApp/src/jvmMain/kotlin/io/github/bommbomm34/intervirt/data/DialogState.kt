package io.github.bommbomm34.intervirt.data

data class DialogState (
    val importance: Importance,
    val message: String,
    val visible: Boolean
){
    companion object {
        val Default = DialogState(
            importance = Importance.INFO,
            message = "",
            visible = false
        )
    }
}

enum class Importance {
    INFO, ERROR, WARNING
}