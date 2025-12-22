package io.github.bommbomm34.intervirt.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
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
    val result: Result<T>? = null,
    val message: String? = null
){
    companion object {
        fun <T> proceed(percentage: Float, message: String? = null) = ResultProgress<T>(percentage, message = message)
        fun <T> failure(exception: Throwable) = result(Result.failure<T>(exception))
        fun <T> success(value: T) = result(Result.success(value))
        fun <T> result(result: Result<T>) = ResultProgress(1f, result)
    }

    fun getResultStatusMessage() = result?.let { if (it.isFailure) "Failed: ${it.exceptionOrNull()!!.message}" else "Success" }
}