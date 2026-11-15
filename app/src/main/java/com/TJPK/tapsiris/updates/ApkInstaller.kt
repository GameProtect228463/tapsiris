package com.TJPK.tapsiris.updates

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Download only after the user accepts; Android performs the final installation confirmation. */
object ApkInstaller {
    suspend fun download(context: Context, release: GitHubRelease): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            require(release.apkUrl.startsWith("https://github.com/GameProtect228463/tapsiris/releases/download/")) {
                "Недопустимый адрес APK"
            }
            val folder = File(context.cacheDir, "updates").apply { mkdirs() }
            val target = File(folder, "tapsiris-update.apk")
            val temp = File(folder, "tapsiris-update.part")
            val connection = URL(release.apkUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.instanceFollowRedirects = true
            try {
                require(connection.responseCode == HttpURLConnection.HTTP_OK) { "HTTP ${connection.responseCode}" }
                connection.inputStream.use { input -> temp.outputStream().use { output -> input.copyTo(output) } }
                require(temp.length() > 0) { "Пустой APK" }
                if (target.exists()) target.delete()
                require(temp.renameTo(target)) { "Не удалось сохранить APK" }
                target
            } finally { connection.disconnect(); temp.delete() }
        }
    }

    fun requestInstall(context: Context, apk: File) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
