package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.readablePercentage

sealed class ResultProgress<T> {
    abstract val percentage: Float

    data class Result<T>(
        override val percentage: Float,
        val result: kotlin.Result<T>
    ) : ResultProgress<T>() {
        fun getResultStatusMessage() =
            if (result.isFailure) "Failed: ${result.exceptionOrNull()!!.message}" else "Success"

        override fun log() = result.fold(
            onSuccess = { "Success" },
            onFailure = { "Failure: ${it.localizedMessage}" }
        )
        override fun message() = result.exceptionOrNull()?.localizedMessage
        override fun clone(percentage: Float) = Result(percentage, result)
    }

    data class Proceed<T>(override val percentage: Float) : ResultProgress<T>() {
        override fun log() = percentage.readablePercentage()
        override fun message() = null
        override fun clone(percentage: Float) = Proceed<T>(percentage)
    }

    data class Message<T>(
        override val percentage: Float,
        val message: String
    ) : ResultProgress<T>() {
        override fun log() = "$message | ${percentage.readablePercentage()}"
        override fun message() = message
        override fun clone(percentage: Float) = Message<T>(percentage, message)
    }

    companion object {
        fun <T> proceed(percentage: Float, message: String? = null) =
            if (message != null) Message<T>(percentage, message) else Proceed(percentage)

        fun <T> failure(exception: Throwable) = result(kotlin.Result.failure<T>(exception))
        fun <T> success(value: T) = result(kotlin.Result.success(value))
        fun <T> result(result: kotlin.Result<T>) = Result(1f, result)
    }

    abstract fun log(): String
    abstract fun message(): String?
    abstract fun clone(percentage: Float): ResultProgress<T>
}