package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.client
import io.github.bommbomm34.intervirt.logger
import io.github.bommbomm34.intervirt.preferences
import io.github.kdownloadfile.downloadFile
import io.ktor.client.request.*
import java.io.File

class FileManagement(val dataDir: File) {
    init {
        preferences.saveString("dataPath", dataDir.absolutePath + File.separator)
        dataDir.createFileInDirectory("qemu", true)
        dataDir.createFileInDirectory("disk", true)
        dataDir.createFileInDirectory("cache", true)
    }

    fun newFile(path: String, directory: Boolean = false): File {
        return dataDir.createFileInDirectory(path)
    }

    fun removeFile(path: String) {
        dataDir.createFileInDirectory(path)
    }

    fun getFile(name: String) = File(dataDir.absolutePath + File.separator + name)

    suspend fun downloadFile(url: String, name: String, destination: File = getFile("cache")): Result<File> {
        logger.debug { "Downloading file $url as $name" }
        val pathRes = downloadFile(
            url = url.getAbsoluteURL(),
            fileName = name,
            folderName = destination.absolutePath,
        )

        pathRes.fold(
            onSuccess = {
                logger.debug { "Downloaded file successfully at $it" }
                return Result.success(File(it))
            },
            onFailure = {
                logger.error { "Download failed: ${it.message}" }
                return Result.failure(it)
            }
        )
    }
}

fun File.createFileInDirectory(name: String, directory: Boolean = false): File {
    if (!isDirectory) error("File $absolutePath must be a directory!")
    val file = File(absolutePath + File.separator + name)
    if (file.exists()) return file
    return file.apply { if (directory) mkdir() else createNewFile() }
}

// If Location Header is provided, this should be used instead as an absolute URL
suspend fun String.getAbsoluteURL() = client.head(this).headers["Location"] ?: this