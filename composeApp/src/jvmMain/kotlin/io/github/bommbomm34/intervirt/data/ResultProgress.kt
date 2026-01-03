package io.github.bommbomm34.intervirt.data

data class ResultProgress <T> (
    val percentage: Float,
    val result: Result<T>? = null,
    val message: String? = result?.exceptionOrNull()?.localizedMessage
){

    companion object {
        fun <T> proceed(percentage: Float, message: String? = null) = ResultProgress<T>(percentage, message = message)
        fun <T> failure(exception: Throwable) = result(Result.failure<T>(exception))
        fun <T> success(value: T) = result(Result.success(value))
        fun <T> result(result: Result<T>) = ResultProgress(1f, result)
    }
    fun getResultStatusMessage() = result?.let { if (it.isFailure) "Failed: ${it.exceptionOrNull()!!.message}" else "Success" }
}