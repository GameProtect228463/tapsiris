package com.TJPK.tapsiris.updates

import android.content.Context

class UpdatePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("github_updates", Context.MODE_PRIVATE)
    var enabled: Boolean
        get() = prefs.getBoolean("enabled", true)
        set(value) { prefs.edit().putBoolean("enabled", value).apply() }
    var repository: String
        get() = prefs.getString("repository", "GameProtect228463/tapsiris") ?: ""
        set(value) { prefs.edit().putString("repository", value).apply() }
}
