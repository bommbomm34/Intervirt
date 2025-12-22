package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.exceptions.OSError
import io.github.bommbomm34.intervirt.exceptions.OperationAlreadyPerformedError
import io.github.bommbomm34.intervirt.exceptions.UndefinedError
import io.github.bommbomm34.intervirt.exceptions.UnknownError

data class ResponseBody (
    val error: String,
    val code: Int
){
    fun exception(): Exception {
        return when (code) {
            1 -> UndefinedError()
            2 -> UnknownError()
            3 -> OperationAlreadyPerformedError()
            4 -> OSError(error)
            0 -> error("No exception available for successful status code 0")
            else -> error("Invalid status code $code")
        }
    }
}