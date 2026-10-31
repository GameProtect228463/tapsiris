package com.TJPK.tapsiris.data.repository

import com.TJPK.tapsiris.data.local.OneCSettingsManager
import com.TJPK.tapsiris.data.mock.MockScheduleData
import com.TJPK.tapsiris.data.remote.OneCApiClient
import com.TJPK.tapsiris.domain.model.BellTime
import com.TJPK.tapsiris.domain.model.DaySchedule
import com.TJPK.tapsiris.domain.model.StudentGroup
import com.TJPK.tapsiris.domain.model.WeekType
import com.TJPK.tapsiris.domain.repository.ScheduleRepository

/**
 * =========================================================================================
 * СЛОЙ DATA / REPOSITORY: Реализация репозитория расписания (Single Source of Truth).
 *
 * Отвечает за:
 * 1. Получение актуальных данных с сервера 1С через [OneCApiClient].
 * 2. Автоматический fallback (переключение) на автономный кэш [MockScheduleData],
 *    если сервер 1С в колледже временно недоступен или включен режим демонстрации.
 * =========================================================================================
 */
class ScheduleRepositoryImpl(
    private val settingsManager: OneCSettingsManager
) : ScheduleRepository {

    private fun getClient(): OneCApiClient {
        val config = settingsManager.getConfig()
        return OneCApiClient(config)
    }

    override suspend fun getGroups(): Result<List<StudentGroup>> {
        val config = settingsManager.getConfig()
        val result = getClient().getGroups()

        return if (result.isSuccess && !result.getOrNull().isNullOrEmpty()) {
            result
        } else if (config.isDemoMode) {
            Result.success(MockScheduleData.groups)
        } else {
            result
        }
    }

    override suspend fun getSchedule(groupId: String, weekType: WeekType): Result<List<DaySchedule>> {
        val config = settingsManager.getConfig()
        val result = getClient().getSchedule(groupId, weekType)

        return if (result.isSuccess && !result.getOrNull().isNullOrEmpty()) {
            result
        } else if (config.isDemoMode) {
            Result.success(MockScheduleData.getWeekSchedule(weekType))
        } else {
            result
        }
    }

    override suspend fun getBells(): Result<List<BellTime>> {
        val config = settingsManager.getConfig()
        val result = getClient().getBells()

        return if (result.isSuccess && !result.getOrNull().isNullOrEmpty()) {
            result
        } else if (config.isDemoMode) {
            Result.success(MockScheduleData.bells)
        } else {
            result
        }
    }

    override suspend fun pingServer(): Result<String> {
        return getClient().ping()
    }
}
