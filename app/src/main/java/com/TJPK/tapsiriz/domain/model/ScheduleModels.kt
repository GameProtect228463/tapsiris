package com.TJPK.tapsiriz.domain.model

/**
 * =========================================================================================
 * СЛОЙ DOMAIN: Чистые сущности и бизнес-модели приложения.
 * Не зависят от UI (Compose/Android Views) и сетевых библиотек.
 * =========================================================================================
 */

/**
 * Тип учебного занятия.
 */
enum class LessonType(val title: String) {
    LECTURE("Лекция"),
    PRACTICE("Практика"),
    LAB("Лабораторная"),
    EXAM("Зачет / Экзамен")
}

/**
 * Одно учебное занятие (пара).
 *
 * @property id Уникальный идентификатор пары
 * @property number Номер пары по порядку (1..5)
 * @property timeStart Время начала в формате "HH:mm"
 * @property timeEnd Время окончания в формате "HH:mm"
 * @property subject Название дисциплины / предмета
 * @property teacher ФИО преподавателя
 * @property room Номер кабинета, аудитории или лаборатории
 * @property type Тип пары (лекция, практика и т.д.)
 * @property isCurrent Флаг: идёт ли пара в текущий момент
 * @property isCanceled Отменена ли пара
 * @property replacementInfo Описание замены (если есть расхождение с базовым расписанием)
 */
data class Lesson(
    val id: String,
    val number: Int,
    val timeStart: String,
    val timeEnd: String,
    val subject: String,
    val teacher: String,
    val room: String,
    val type: LessonType,
    val isCurrent: Boolean = false,
    val isCanceled: Boolean = false,
    val replacementInfo: String? = null
)

/**
 * Расписание занятий на конкретный день недели.
 *
 * @property dayIndex Порядковый номер дня недели (1 = Пн, 2 = Вт ... 6 = Сб)
 * @property dayShortName Сокращенное название (Пн, Вт, Ср...)
 * @property dayFullName Полное название (Понедельник...)
 * @property dateText Дата в читаемом виде (например, "29 окт.")
 * @property isToday Является ли этот день текущим календарным днем
 * @property lessons Список пар на этот день
 */
data class DaySchedule(
    val dayIndex: Int,
    val dayShortName: String,
    val dayFullName: String,
    val dateText: String,
    val isToday: Boolean,
    val lessons: List<Lesson>
) {
    /** Есть ли хотя бы одна замена в этот день */
    val hasReplacements: Boolean
        get() = lessons.any { it.replacementInfo != null }
}

/**
 * Тип учебной недели (Чётность).
 */
enum class WeekType(val title: String) {
    ODD("Числитель"),
    EVEN("Знаменатель")
}

/**
 * Учебная группа колледжа.
 *
 * @property id Идентификатор группы в 1С (код или GUID)
 * @property name Название группы (например: "ИС-21")
 * @property course Номер курса (1..4)
 * @property department Специальность / отделение
 */
data class StudentGroup(
    val id: String,
    val name: String,
    val course: Int,
    val department: String
)

/**
 * Элемент расписания звонков колледжа.
 *
 * @property number Номер пары
 * @property timeStart Время начала пары
 * @property timeEnd Время конца пары
 * @property breakMinutes Длительность перерыва после этой пары
 */
data class BellTime(
    val number: Int,
    val timeStart: String,
    val timeEnd: String,
    val breakMinutes: Int
)
