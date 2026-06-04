package io.github.bommbomm34.intervirt.core.data

import arrow.core.Either
import io.github.bommbomm34.intervirt.core.CURRENT_VERSION
import io.github.bommbomm34.intervirt.core.data.qemu.QmpErrorBody
import io.github.bommbomm34.intervirt.core.exceptions.CommandExecutionException

sealed class Failure(val message: String) {
    companion object {
        inline fun <R> catch(block: () -> R): Either<Unexpected, R> {
            return Either.catch(block).mapLeft(::Unexpected)
        }
    }

    sealed class Agent(
        message: String,
        val uuid: String?,
    ) : Failure("Agent failure during request $uuid: $message")

    class OperationAlreadyPerformed(
        message: String? = null,
        uuid: String? = null,
    ) : Agent("Operation already performed: ${message ?: "Unknown reason"}", uuid)

    class Undefined(
        message: String,
        uuid: String? = null,
    ) : Agent("Undefined failure: $message", uuid)

    class Unknown(uuid: String? = null) : Agent("Unknown failure.", uuid)

    class OS(
        message: String,
        uuid: String? = null,
    ) : Agent("OS failure: $message", uuid)

    class ContainerExecution(
        message: String,
        uuid: String? = null,
    ) : Agent("Container execution failure: $message", uuid)

    class NotFound(
        message: String,
        uuid: String? = null,
    ) : Agent("Not found: $message", uuid)

    class NotSupportedOperation(
        message: String,
        uuid: String? = null,
    ) : Agent("Not supported operation: $message", uuid)

    class IllegalArgument(
        message: String,
        uuid: String? = null,
    ) : Agent("Illegal argument: $message", uuid)

    class AgentTimeout(uuid: String? = null) : Agent("Agent exceeded timeout", uuid)

    class IllegalAgentResponse(
        message: String,
        uuid: String? = null,
    ) : Agent("Illegal agent response: $message", uuid)

    class Unexpected(val exception: Throwable) : Failure(exception.message ?: "Unknown")

    class Qmp(body: QmpErrorBody) : Failure(body.description)

    class Serialization(message: String) : Failure("Serialization failure: $message")

    class IllegalState(message: String) : Failure("Illegal state: $message")

    class Download(message: String) : Failure("Download failure: $message")

    class ZipExtraction(
        val filename: String,
        message: String? = null,
    ) : Failure("ZIP extraction failure: ${message ?: "Unknwon reason"}")

    class CommandExecution(
        statusCode: Int,
        output: String,
    ) : Failure("Command failed with status code $statusCode: $output")

    class InvalidMail(message: String) : Failure("Invalid mail: $message")

    class VersionMismatch(other: String) : Failure("Version $other doesn't match current version $CURRENT_VERSION")
}

typealias AppResult<T> = Either<Failure, T>
