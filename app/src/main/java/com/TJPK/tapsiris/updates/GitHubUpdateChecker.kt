package com.TJPK.tapsiris.updates

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Only official GitHub release metadata is used; installation remains user-controlled. */
data class GitHubRelease(val version: String, val apkUrl: String, val pageUrl: String)

object GitHubUpdateChecker {
    suspend fun check(context: Context, repository: String): Result<GitHubRelease?> = withContext(Dispatchers.IO) {
        runCatching {
            require(Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+").matches(repository)) { "Укажите owner/repo" }
            val connection = URL("https://api.github.com/repos/$repository/releases/latest").openConnection() as HttpURLConnection
            connection.connectTimeout = 7000
            connection.readTimeout = 7000
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "Tapsiris-Android")
            try {
                require(connection.responseCode == 200) { "GitHub HTTP ${connection.responseCode}" }
                val release = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                val tag = release.getString("tag_name")
                val assets = release.optJSONArray("assets")
                var apk = ""
                if (assets != null) for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.optString("name").endsWith(".apk", true)) {
                        apk = asset.optString("browser_download_url"); break
                    }
                }
                val current = context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
                val remote = tag.removePrefix("v")
                // Strict comparison: no false update prompts for non-semver tags.
                val newer = compareVersions(remote, current) > 0
                if (newer && apk.startsWith("https://github.com/")) GitHubRelease(tag, apk, release.getString("html_url")) else null
            } finally { connection.disconnect() }
        }
    }

    private fun compareVersions(a: String, b: String): Int {
        fun parts(v: String) = Regex("""^(\d+)\.(\d+)\.(\d+)""").find(v)?.groupValues?.drop(1)?.map { it.toIntOrNull() ?: 0 }
        val x = parts(a) ?: return 0
        val y = parts(b) ?: return 0
        for (i in 0..2) if (x[i] != y[i]) return x[i].compareTo(y[i])
        return 0
    }

    fun openRelease(context: Context, release: GitHubRelease) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(release.pageUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
