package io.github.bommbomm34.intervirt.gui.components.configuration

import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import intervirt.composeapp.generated.resources.*
import io.github.bommbomm34.intervirt.core.data.VMConfigurationData
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun DiskUrlConfiguration(
    conf: VMConfigurationData,
    onConfChange: (VMConfigurationData) -> Unit
){
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val client = koinInject<HttpClient>()
    val appState = koinInject<AppState>()
    OutlinedTextField(
        value = conf.diskUrl,
        onValueChange = { onConfChange(conf.copy(diskUrl = it)) },
        label = { Text(stringResource(Res.string.vm_disk_url)) }
    )
    GeneralSpacer()
    OutlinedTextField(
        value = conf.diskHashUrl,
        onValueChange = { onConfChange(conf.copy(diskHashUrl = it)) },
        label = { Text(stringResource(Res.string.vm_disk_hash_url)) }
    )
    GeneralSpacer()
    Button(
        onClick = {
            scope.launch {
                client.validate(conf.diskUrl){
                    appState.openDialog(
                        importance = Importance.ERROR,
                        message = getString(Res.string.disk_url_validation_failure, it)
                    )
                }
                client.validate(conf.diskHashUrl){
                    appState.openDialog(
                        importance = Importance.ERROR,
                        message = getString(Res.string.disk_hash_url_validation_failure, it)
                    )
                }
            }
        }
    ){
        Text(stringResource(Res.string.validate_urls))
    }
}

private suspend inline fun HttpClient.validate(
    url: String,
    onFailure: (String) -> Unit
){
    head(url).status.let {
        if (it != HttpStatusCode.OK) onFailure(it.description)
    }
}