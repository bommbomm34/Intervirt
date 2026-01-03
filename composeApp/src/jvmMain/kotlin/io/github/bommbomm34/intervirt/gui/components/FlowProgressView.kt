package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import io.github.bommbomm34.intervirt.data.ResultProgress
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow

@Composable
fun <T> FlowProgressView(
    flow: Flow<ResultProgress<T>>,
    onMessage: ((ResultProgress<T>) -> Unit)? = null
) {
    val defaultMessageColor = MaterialTheme.colors.onBackground
    var message by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0f) }
    var messageColor by remember { mutableStateOf(defaultMessageColor) }
    
    LaunchedEffect(Unit) {
        flow.collect { resultProgress ->
            messageColor = resultProgress.result?.let{ if (it.isSuccess) Color.Green else Color.Red } ?: defaultMessageColor
            message = resultProgress.result?.exceptionOrNull()?.localizedMessage ?: resultProgress.message ?: ""
            progress = resultProgress.percentage
            onMessage?.invoke(resultProgress)
        }
    }
    ProgressView(
        progress = progress,
        message = message,
        messageColor = messageColor
    )
}
