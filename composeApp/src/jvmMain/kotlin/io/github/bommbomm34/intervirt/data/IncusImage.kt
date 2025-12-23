package io.github.bommbomm34.intervirt.data

data class IncusImage(
    val name: String,
    val tag: String
){
    fun fullName() = "$name/$tag"
}