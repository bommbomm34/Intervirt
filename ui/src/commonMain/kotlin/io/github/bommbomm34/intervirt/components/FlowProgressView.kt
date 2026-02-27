package io.github.bommbomm34.intervirt.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.job

@Composable
fun <T> FlowProgressView(
    flow: Flow<ResultProgress<T>>,
    onJobChange: ((Job?) -> Unit),
    onMessage: ((ResultProgress<T>) -> Unit)? = null,
) {
    val defaultMessageColor = MaterialTheme.colorScheme.onBackground
    var message by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0f) }
    var messageColor by remember { mutableStateOf(defaultMessageColor) }

    LaunchedEffect(Unit) {
        onJobChange(coroutineContext.job)
        flow.collect { resultProgress ->
            messageColor =
                if (resultProgress is ResultProgress.Result) if (resultProgress.result.isSuccess) Color.Green else Color.Red else defaultMessageColor
            message = resultProgress.message() ?: ""
            progress = resultProgress.percentage
            onMessage?.invoke(resultProgress)
        }
        onJobChange(null)
    }

    ProgressView(
        progress = progress,
        message = message,
        messageColor = messageColor,
    )
}
