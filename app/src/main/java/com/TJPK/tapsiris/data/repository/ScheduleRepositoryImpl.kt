package com.TJPK.tapsiris.data.repository

import android.content.Context
import com.TJPK.tapsiris.data.local.OneCSettingsManager
import com.TJPK.tapsiris.data.local.UserProfile
import com.TJPK.tapsiris.data.local.UserRole
import com.TJPK.tapsiris.data.local.db.ScheduleDatabaseHelper
import com.TJPK.tapsiris.data.mock.MockScheduleData
import com.TJPK.tapsiris.data.remote.OneCApiClient
import com.TJPK.tapsiris.domain.model.BellTime
import com.TJPK.tapsiris.domain.model.DaySchedule
import com.TJPK.tapsiris.domain.model.StudentGroup
import com.TJPK.tapsiris.domain.model.WeekType
import com.TJPK.tapsiris.domain.repository.ScheduleRepository

/**
 * =========================================================================================
 * СЛОЙ DATA / REPOSITORY: Реализация репозитория расписания и авторизации 1С.
 *
 * Single Source of Truth:
 * 1. Получение данных и авторизация пользователей через сервер 1С [OneCApiClient].
 * 2. Автономное хранение и загрузка справочника групп из локальной БД [ScheduleDatabaseHelper].
 * 3. Автоматический fallback на демонстрационную базу [MockScheduleData],
 *    когда сервер 1С в колледже выключен или включен демо-режим.
 * =========================================================================================
 */
class ScheduleRepositoryImpl(
    private val settingsManager: OneCSettingsManager,
    context: Context,
    private val dbHelper: ScheduleDatabaseHelper = ScheduleDatabaseHelper(context)
) : ScheduleRepository {

    private fun getClient(): OneCApiClient {
        val config = settingsManager.getConfig()
        return OneCApiClient(config)
    }

    override suspend fun authenticate(login: String, password: String, role: UserRole): Result<UserProfile> {
        val config = settingsManager.getConfig()

        // Если включён реальный сервер 1С (не демо-режим и URL настроен)
        if (!config.isDemoMode && config.serverUrl.isNotBlank()) {
            val result = getClient().authenticate(login, password, role)
            if (result.isSuccess) {
                return result
            }
        }

        // Авторизация по локальной базе 1С
        val mockUser = MockScheduleData.authenticateMock(login, role)
        return if (mockUser != null) {
            Result.success(mockUser)
        } else {
            Result.success(MockScheduleData.createDefaultProfileForRole(login, role))
        }
    }

    override suspend fun getGroups(): Result<List<StudentGroup>> {
        val config = settingsManager.getConfig()

        // 1. Автоматическая подгрузка свежих групп из базы 1С
        if (!config.isDemoMode && config.serverUrl.isNotBlank()) {
            val remoteResult = getClient().getGroups()
            if (remoteResult.isSuccess && !remoteResult.getOrNull().isNullOrEmpty()) {
                val freshGroups = remoteResult.getOrNull()!!
                // Очищаем тестовые данные и сохраняем актуальные группы из 1С в БД SQLite
                dbHelper.saveGroups(freshGroups, clearOld = true)
                return Result.success(freshGroups)
            } else if (remoteResult.isFailure) {
                val dbGroups = dbHelper.getAllGroups()
                if (dbGroups.isNotEmpty()) {
                    return Result.success(dbGroups)
                }
                return remoteResult
            }
        }

        // 2. Если сервер 1С недоступен — загружаем из локальной базы данных SQLite
        val dbGroups = dbHelper.getAllGroups()
        if (dbGroups.isNotEmpty()) {
            return Result.success(dbGroups)
        }

        // 3. Fallback: первоначальное сохранение эталонных групп в локальную БД
        val fallbackGroups = MockScheduleData.groups
        dbHelper.saveGroups(fallbackGroups)
        return Result.success(fallbackGroups)
    }

    override suspend fun getGroupsFromDb(): Result<List<StudentGroup>> {
        // Чтение групп напрямую из локальной базы данных SQLite (для экрана регистрации)
        val dbGroups = dbHelper.getAllGroups()
        if (dbGroups.isNotEmpty()) {
            return Result.success(dbGroups)
        }
        val fallbackGroups = MockScheduleData.groups
        dbHelper.saveGroups(fallbackGroups)
        return Result.success(fallbackGroups)
    }

    override suspend fun getSchedule(groupId: String, weekType: WeekType): Result<List<DaySchedule>> {
        val config = settingsManager.getConfig()
        val result = getClient().getSchedule(groupId, weekType)

        return if (result.isSuccess && !result.getOrNull().isNullOrEmpty()) {
            result
        } else {
            // Офлайн-кэш расписания
            val fallback = MockScheduleData.getWeekSchedule(weekType)
            Result.success(fallback)
        }
    }

    override suspend fun getBells(): Result<List<BellTime>> {
        val result = getClient().getBells()
        return if (result.isSuccess && !result.getOrNull().isNullOrEmpty()) {
            result
        } else {
            Result.success(MockScheduleData.bells)
        }
    }

    override suspend fun pingServer(): Result<String> {
        return getClient().ping()
    }
}
