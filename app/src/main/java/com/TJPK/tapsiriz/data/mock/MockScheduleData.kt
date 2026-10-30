package com.TJPK.tapsiriz.data.mock

import com.TJPK.tapsiriz.domain.model.BellTime
import com.TJPK.tapsiriz.domain.model.DaySchedule
import com.TJPK.tapsiriz.domain.model.Lesson
import com.TJPK.tapsiriz.domain.model.LessonType
import com.TJPK.tapsiriz.domain.model.StudentGroup
import com.TJPK.tapsiriz.domain.model.WeekType

/**
 * =========================================================================================
 * СЛОЙ DATA / MOCK: Автономные демонстрационные данные и резервный кэш.
 * Используются для предпросмотра (Compose Preview), тестирования и офлайн-режима,
 * когда сервер 1С в колледже выключен или недоступен из внешней сети.
 * =========================================================================================
 */
object MockScheduleData {

    val groups = listOf(
        StudentGroup("is21", "ИС-21", 2, "Информационные системы"),
        StudentGroup("is31", "ИС-31", 3, "Информационные системы"),
        StudentGroup("p22", "П-22", 2, "Программная инженерия"),
        StudentGroup("vt11", "ВТ-11", 1, "Вычислительная техника"),
        StudentGroup("tj32", "ТЖ-32", 3, "Техническая эксплуатация"),
        StudentGroup("b41", "БУХ-41", 4, "Экономика и учет")
    )

    val bells = listOf(
        BellTime(1, "08:30", "10:00", 10),
        BellTime(2, "10:10", "11:40", 40), // Обед / большой перерыв
        BellTime(3, "12:20", "13:50", 15),
        BellTime(4, "14:05", "15:35", 10),
        BellTime(5, "15:45", "17:15", 10)
    )

    fun getWeekSchedule(weekType: WeekType): List<DaySchedule> {
        return listOf(
            DaySchedule(
                dayIndex = 1,
                dayShortName = "Пн",
                dayFullName = "Понедельник",
                dateText = "27 окт.",
                isToday = false,
                lessons = listOf(
                    Lesson(
                        id = "m1",
                        number = 1,
                        timeStart = "08:30",
                        timeEnd = "10:00",
                        subject = "Разработка мобильных приложений",
                        teacher = "Калиев А. С.",
                        room = "каб. 308 (Корпус 1)",
                        type = LessonType.PRACTICE
                    ),
                    Lesson(
                        id = "m2",
                        number = 2,
                        timeStart = "10:10",
                        timeEnd = "11:40",
                        subject = "Архитектура компьютерных систем",
                        teacher = "Сергеева М. В.",
                        room = "ауд. 214",
                        type = LessonType.LECTURE
                    ),
                    Lesson(
                        id = "m3",
                        number = 3,
                        timeStart = "12:20",
                        timeEnd = "13:50",
                        subject = "Базы данных и SQL",
                        teacher = "Нурланов Б. К.",
                        room = "комп. класс 402",
                        type = LessonType.LAB,
                        replacementInfo = "Замена преподавателя (вместо Ахметова Д.)"
                    )
                )
            ),
            DaySchedule(
                dayIndex = 2,
                dayShortName = "Вт",
                dayFullName = "Вторник",
                dateText = "28 окт.",
                isToday = false,
                lessons = listOf(
                    Lesson(
                        id = "tu1",
                        number = 1,
                        timeStart = "08:30",
                        timeEnd = "10:00",
                        subject = "Высшая математика",
                        teacher = "Иванова Е. П.",
                        room = "каб. 105",
                        type = LessonType.LECTURE
                    ),
                    Lesson(
                        id = "tu2",
                        number = 2,
                        timeStart = "10:10",
                        timeEnd = "11:40",
                        subject = "Операционные системы",
                        teacher = "Сериков Т. Е.",
                        room = "каб. 210",
                        type = LessonType.LAB
                    ),
                    Lesson(
                        id = "tu3",
                        number = 3,
                        timeStart = "12:20",
                        timeEnd = "13:50",
                        subject = "Иностранный язык (профессиональный)",
                        teacher = "Смирнова О. Н.",
                        room = "каб. 315",
                        type = LessonType.PRACTICE
                    ),
                    Lesson(
                        id = "tu4",
                        number = 4,
                        timeStart = "14:05",
                        timeEnd = "15:35",
                        subject = "Физическая культура",
                        teacher = "Жумабаев Р. Т.",
                        room = "Спортзал",
                        type = LessonType.PRACTICE
                    )
                )
            ),
            DaySchedule(
                dayIndex = 3,
                dayShortName = "Ср",
                dayFullName = "Среда",
                dateText = "29 окт.",
                isToday = true,
                lessons = listOf(
                    Lesson(
                        id = "w1",
                        number = 1,
                        timeStart = "08:30",
                        timeEnd = "10:00",
                        subject = "Веб-технологии и frontend",
                        teacher = "Омаров Д. А.",
                        room = "комп. класс 405",
                        type = LessonType.LAB
                    ),
                    Lesson(
                        id = "w2",
                        number = 2,
                        timeStart = "10:10",
                        timeEnd = "11:40",
                        subject = "Разработка мобильных приложений (Android)",
                        teacher = "Калиев А. С.",
                        room = "каб. 308",
                        type = LessonType.PRACTICE,
                        isCurrent = true,
                        replacementInfo = "Срочный перенос из ауд. 102 в каб. 308!"
                    ),
                    Lesson(
                        id = "w3",
                        number = 3,
                        timeStart = "12:20",
                        timeEnd = "13:50",
                        subject = "Компьютерные сети",
                        teacher = "Бекенов А. М.",
                        room = "ауд. 215",
                        type = LessonType.LECTURE
                    ),
                    Lesson(
                        id = "w4",
                        number = 4,
                        timeStart = "14:05",
                        timeEnd = "15:35",
                        subject = "Кураторский час / Собрание",
                        teacher = "Сергеева М. В.",
                        room = "каб. 214",
                        type = LessonType.PRACTICE
                    )
                )
            ),
            DaySchedule(
                dayIndex = 4,
                dayShortName = "Чт",
                dayFullName = "Четверг",
                dateText = "30 окт.",
                isToday = false,
                lessons = listOf(
                    Lesson(
                        id = "th1",
                        number = 1,
                        timeStart = "08:30",
                        timeEnd = "10:00",
                        subject = "Информационная безопасность",
                        teacher = "Ким В. В.",
                        room = "ауд. 312",
                        type = LessonType.LECTURE
                    ),
                    Lesson(
                        id = "th2",
                        number = 2,
                        timeStart = "10:10",
                        timeEnd = "11:40",
                        subject = "Информационная безопасность",
                        teacher = "Ким В. В.",
                        room = "комп. класс 401",
                        type = LessonType.LAB
                    ),
                    Lesson(
                        id = "th3",
                        number = 3,
                        timeStart = "12:20",
                        timeEnd = "13:50",
                        subject = "Алгоритмы и структуры данных",
                        teacher = "Нурланов Б. К.",
                        room = "каб. 204",
                        type = LessonType.PRACTICE
                    )
                )
            ),
            DaySchedule(
                dayIndex = 5,
                dayShortName = "Пт",
                dayFullName = "Пятница",
                dateText = "31 окт.",
                isToday = false,
                lessons = listOf(
                    Lesson(
                        id = "f1",
                        number = 1,
                        timeStart = "08:30",
                        timeEnd = "10:00",
                        subject = "Проектный практикум",
                        teacher = "Калиев А. С.",
                        room = "Лаборатория IT",
                        type = LessonType.PRACTICE
                    ),
                    Lesson(
                        id = "f2",
                        number = 2,
                        timeStart = "10:10",
                        timeEnd = "11:40",
                        subject = "Инженерная графика и дизайн",
                        teacher = "Петров С. Н.",
                        room = "каб. 218",
                        type = LessonType.PRACTICE
                    ),
                    Lesson(
                        id = "f3",
                        number = 3,
                        timeStart = "12:20",
                        timeEnd = "13:50",
                        subject = "Основы философии и права",
                        teacher = "Мукашев С. А.",
                        room = "ауд. 101",
                        type = LessonType.LECTURE
                    )
                )
            ),
            DaySchedule(
                dayIndex = 6,
                dayShortName = "Сб",
                dayFullName = "Суббота",
                dateText = "1 нояб.",
                isToday = false,
                lessons = if (weekType == WeekType.ODD) {
                    listOf(
                        Lesson(
                            id = "s1",
                            number = 1,
                            timeStart = "08:30",
                            timeEnd = "10:00",
                            subject = "Факультатив: Kotlin / Android Jetpack",
                            teacher = "Калиев А. С.",
                            room = "комп. класс 402",
                            type = LessonType.PRACTICE
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )
    }
}
