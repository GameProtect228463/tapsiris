package com.TJPK.tapsiris.domain.repository

import com.TJPK.tapsiris.data.local.UserProfile
import com.TJPK.tapsiris.data.local.UserRole
import com.TJPK.tapsiris.domain.model.BellTime
import com.TJPK.tapsiris.domain.model.DaySchedule
import com.TJPK.tapsiris.domain.model.StudentGroup
import com.TJPK.tapsiris.domain.model.WeekType

/**
 * =========================================================================================
 * СЛОЙ DOMAIN: Контракт репозитория расписания.
 * Определяет ЧТО умеет делать приложение с данными, скрывая детали того, КАК они получаются.
 * Включает методы для работы с расписанием, группами, звонками и учетными записями 1С.
 * =========================================================================================
 */
interface ScheduleRepository {

    /**
     * Получить список всех учебных групп колледжа (с автоматической синхронизацией из базы 1С).
     */
    suspend fun getGroups(): Result<List<StudentGroup>>

    /**
     * Получить список групп напрямую из локальной базы данных SQLite (для быстрой регистрации и офлайн-доступа).
     */
    suspend fun getGroupsFromDb(): Result<List<StudentGroup>>

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

    /**
     * Авторизация пользователя в базе 1С (Студент, Преподаватель, Администратор).
     */
    suspend fun authenticate(login: String, password: String, role: UserRole): Result<UserProfile>

    // ─── Функции Администратора (Синхронизация с 1С) ─────────────────────────

    /**
     * Создать или обновить учебную группу в 1С и локальной базе SQLite.
     */
    suspend fun createGroup(group: StudentGroup): Result<String>

    /**
     * Удалить группу в 1С и локальной базе.
     */
    suspend fun deleteGroup(groupId: String): Result<String>

    /**
     * Добавить или изменить пару в расписании группы в 1С.
     */
    suspend fun saveLesson(
        groupId: String,
        weekType: WeekType,
        dayIndex: Int,
        lesson: com.TJPK.tapsiris.domain.model.Lesson
    ): Result<String>

    /**
     * Удалить пару из расписания 1С.
     */
    suspend fun deleteLesson(
        groupId: String,
        weekType: WeekType,
        dayIndex: Int,
        lessonId: String
    ): Result<String>

    // ─── Функции Преподавателя (Синхронизация с 1С) ──────────────────────────

    /**
     * Сменить аудиторию / кабинет пары в 1С.
     */
    suspend fun changeRoom(
        groupId: String,
        weekType: WeekType,
        dayIndex: Int,
        lessonId: String,
        newRoom: String
    ): Result<String>

    /**
     * Назначить замену для пары в 1С.
     */
    suspend fun setReplacement(
        groupId: String,
        weekType: WeekType,
        dayIndex: Int,
        lessonId: String,
        replacementInfo: String,
        newTeacher: String? = null,
        newSubject: String? = null
    ): Result<String>

    /**
     * Предупредить о задержке занятия (в минутах с комментарием).
     */
    suspend fun notifyDelay(
        groupId: String,
        weekType: WeekType,
        dayIndex: Int,
        lessonId: String,
        delayMinutes: Int,
        delayNotice: String
    ): Result<String>
}
