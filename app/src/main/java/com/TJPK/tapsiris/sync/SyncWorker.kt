package com.TJPK.tapsiris.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.TJPK.tapsiris.data.local.OneCSettingsManager
import com.TJPK.tapsiris.data.repository.ScheduleRepositoryImpl

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (!applicationContext.getSharedPreferences("tapsiris_sync", Context.MODE_PRIVATE).getBoolean("enabled", true)) return Result.success()
        val settings = OneCSettingsManager(applicationContext)
        val config = settings.getConfig()
        if (config.isDemoMode || config.serverUrl.isBlank()) return Result.success()
        val repository = ScheduleRepositoryImpl(settings, applicationContext)
        val groups = repository.getGroups()
        val bells = repository.getBells()
        return if (groups.isSuccess && bells.isSuccess) Result.success() else Result.retry()
    }
}
