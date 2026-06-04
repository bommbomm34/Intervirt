/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

import arrow.core.left
import arrow.core.right
import io.github.bommbomm34.intervirt.core.util.ext.readablePercentage

sealed class ResultProgress<out T> {
    abstract val percentage: Float

    data class Result<out T>(
        override val percentage: Float,
        val result: AppResult<T>,
    ) : ResultProgress<T>() {
        override fun log() = result.fold(
            ifRight = { "Success" },
            ifLeft = { "Failure: ${it.message}" },
        )

        override fun message() = result.leftOrNull()?.message
        override fun clone(percentage: Float) = Result(percentage, result)
    }

    data class Proceed<out T>(override val percentage: Float) : ResultProgress<T>() {
        override fun log() = percentage.readablePercentage()
        override fun message() = null
        override fun clone(percentage: Float) = Proceed<T>(percentage)
    }

    data class Message<out T>(
        override val percentage: Float,
        val message: String,
    ) : ResultProgress<T>() {
        override fun log() = "$message | ${percentage.readablePercentage()}"
        override fun message() = message
        override fun clone(percentage: Float) = Message<T>(percentage, message)
    }

    companion object {
        fun <T> proceed(percentage: Float, message: String? = null) =
            if (message != null) Message<T>(percentage, message) else Proceed(percentage)

        fun <T> failure(failure: Failure): Result<T> = result(failure.left())
        fun <T> success(value: T) = result(value.right())
        fun <T> result(result: AppResult<T>) = Result(1f, result)
    }

    abstract fun log(): String
    abstract fun message(): String?
    abstract fun clone(percentage: Float): ResultProgress<T>
}
