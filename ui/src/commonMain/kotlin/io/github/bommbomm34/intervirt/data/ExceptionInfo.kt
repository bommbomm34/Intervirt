package io.github.bommbomm34.intervirt.data

import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.core.exceptions.*
import org.jetbrains.compose.resources.getString
import java.io.IOException

data class ExceptionInfo(
    val message: String,
    val exception: Throwable,
)

suspend fun Throwable.parseException(): ExceptionInfo {
    val internalError = getString(Res.string.internal_error_occurred, localizedMessage)
    val msg = when (this) {
        is IOException -> getString(Res.string.io_error_occurred, localizedMessage)
        is AgentTimeoutException -> getString(Res.string.agent_timeout_occurred, localizedMessage)
        is ContainerExecutionException -> getString(Res.string.container_command_failed, localizedMessage)
        is DeprecatedException -> getString(Res.string.deprecated_agent, localizedMessage)
        is DownloadException -> getString(Res.string.download_failed, localizedMessage)
        is InvalidMailException -> getString(Res.string.received_mail_is_invalid, localizedMessage)
        is AgentException -> internalError
        is QmpException -> internalError
        is UnhealthyDockerContainerException -> internalError
        is UnsupportedArchitectureException -> getString(
            Res.string.arch_is_not_supported,
            System.getProperty("os.arch"),
        )

        is UnsupportedOsException -> getString(Res.string.os_is_not_supported, System.getProperty("os.name"))
        is ZipExtractionException -> getString(Res.string.error_while_zip_extraction, filename, errorMessage)
        else -> getString(Res.string.undefined_error_occurred, localizedMessage)
    }
    return ExceptionInfo(
        message = msg,
        exception = this,
    )
}