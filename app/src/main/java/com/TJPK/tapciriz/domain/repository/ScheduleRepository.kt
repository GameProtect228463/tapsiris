package com.TJPK.tapciriz.domain.repository

import com.TJPK.tapciriz.domain.model.BellTime
import com.TJPK.tapciriz.domain.model.DaySchedule
import com.TJPK.tapciriz.domain.model.StudentGroup
import com.TJPK.tapciriz.domain.model.WeekType

/**
 * =========================================================================================
 * СЛОЙ DOMAIN: Контракт репозитория расписания.
 * Определяет ЧТО умеет делать приложение с данными, скрывая детали того, КАК они получаются.
 * =========================================================================================
 */
interface ScheduleRepository {

    /**
     * Получить список всех учебных групп колледжа.
     */
    suspend fun getGroups(): Result<List<StudentGroup>>

    /**
     * Получить недельное расписание для конкретной группы и типа недели.
     *
     * @param groupId Идентификатор группы (из справочника 1С)
     * @param weekType Числитель или Знаменатель
     */
    suspend fun getSchedule(groupId: String, weekType: WeekType): Result<List<DaySchedule>>

    /**
     * Получить расписание звонков колледжа (пары и перемены).
     */
    suspend fun getBells(): Result<List<BellTime>>

    /**
     * Проверить доступность сервера 1С.
     */
    suspend fun pingServer(): Result<String>
}
