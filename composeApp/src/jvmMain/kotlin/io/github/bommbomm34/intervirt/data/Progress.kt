package io.github.bommbomm34.intervirt.data

import org.jetbrains.compose.resources.StringResource

data class Progress (
    val percentage: Float,
    val message: String,
    val successful: Boolean? = null
){
    companion object {
        fun error(message: String) = Progress(1f, message, false)
        fun success(message: String) = Progress(1f, message, true)
    }
}

data class ResultProgress <T> (
    val percentage: Float,
    val result: Result<T>? = null
){
    companion object {
        fun <T> proceed(percentage: Float) = ResultProgress<T>(percentage)
        fun <T> result(result: Result<T>) = ResultProgress<T>(1f, result)
    }
}