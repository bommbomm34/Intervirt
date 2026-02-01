package io.github.bommbomm34.intervirt.gui

import androidx.compose.runtime.Composable
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice

@Composable
fun ShellView(computer: ViewDevice.Computer) {
//    val executor = koinInject<Executor>()
//    val appState = koinInject<AppState>()
//    val appEnv = koinInject<AppEnv>()
//    val navigator = rememberWebViewNavigator()
//    val logger = remember { KotlinLogging.logger { } }
//    var ready by remember { mutableStateOf(false) }
//    if (appEnv.enableAgent) {
//        var session: RemoteContainerSession? by remember { mutableStateOf(null) }
//        LaunchedEffect(computer.id) {
//            val result = executor.getContainerSession(computer.id)
//            result.fold(
//                onSuccess = {
//                    session = it
//                    ready = true
//                },
//                onFailure = {
//                    appState.openDialog(
//                        importance = Importance.ERROR,
//                        message = it.localizedMessage
//                    )
//                }
//            )
//        }
//    }
//
//    Column {
//        WebView(
//            state = rememberWebViewState(""),
//            modifier = Modifier.fillMaxSize(),
//            navigator = navigator
//        )
//    }
}