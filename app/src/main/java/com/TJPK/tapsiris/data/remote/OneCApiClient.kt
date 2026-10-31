package com.TJPK.tapsiris.data.remote

import android.util.Base64
import com.TJPK.tapsiris.domain.model.BellTime
import com.TJPK.tapsiris.domain.model.DaySchedule
import com.TJPK.tapsiris.domain.model.Lesson
import com.TJPK.tapsiris.domain.model.LessonType
import com.TJPK.tapsiris.domain.model.StudentGroup
import com.TJPK.tapsiris.domain.model.WeekType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * =========================================================================================
 * СЛОЙ DATA / REMOTE: Сетевой клиент взаимодействия с сервером 1С:Предприятие.
 *
 * Отвечает за:
 * 1. Формирование HTTP-соединения и авторизацию (Basic Auth логин/пароль 1С).
 * 2. Выполнение сетевых запросов в фоновом потоке (Dispatchers.IO).
 * 3. Десериализацию JSON-ответов сервера 1С в чистые доменные модели (Domain Models).
 * =========================================================================================
 */
class OneCApiClient(private val config: ServerConfig) {

    private fun createConnection(endpointUrl: String): HttpURLConnection {
        val url = URL(endpointUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 7000
        conn.readTimeout = 7000
        conn.requestMethod = "GET"
        conn.setRequestProperty("Accept", "application/json; charset=utf-8")
        conn.setRequestProperty("User-Agent", "Tapciriz-Android-Client/1.0")

        if (config.username1C.isNotBlank()) {
            val credentials = "${config.username1C}:${config.password1C}"
            val authHeader = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
            conn.setRequestProperty("Authorization", authHeader)
        }
        return conn
    }

    /**
     * Проверка доступности веб-сервиса 1С.
     * Запрашивает конечную точку /ping.
     */
    suspend fun ping(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val trimmedUrl = config.serverUrl.trimEnd('/')
            val conn = createConnection("$trimmedUrl/ping")
            val code = conn.responseCode
            if (code in 200..299) {
                Result.success("Соединение с 1С успешно установлено! (Код $code)")
            } else {
                val errorStream = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Result.failure(Exception("Сервер 1С вернул код $code. $errorStream"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Не удалось связаться с 1С: ${e.localizedMessage}"))
        }
    }

    /**
     * Получение актуального списка групп из справочника 1С.
     */
    suspend fun getGroups(): Result<List<StudentGroup>> = withContext(Dispatchers.IO) {
        try {
            val trimmedUrl = config.serverUrl.trimEnd('/')
            val conn = createConnection("$trimmedUrl/groups")
            val code = conn.responseCode
            if (code in 200..299) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val groups = parseGroupsJson(responseText)
                Result.success(groups)
            } else {
                Result.failure(Exception("Ошибка 1С (код $code)"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Получение расписания для группы и недели из регистра сведений / документов 1С.
     */
    suspend fun getSchedule(groupId: String, weekType: WeekType): Result<List<DaySchedule>> =
        withContext(Dispatchers.IO) {
            try {
                val trimmedUrl = config.serverUrl.trimEnd('/')
                val weekQuery = if (weekType == WeekType.ODD) "odd" else "even"
                val conn = createConnection("$trimmedUrl/schedule?groupId=$groupId&week=$weekQuery")
                val code = conn.responseCode
                if (code in 200..299) {
                    val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                    val days = parseScheduleJson(responseText)
                    Result.success(days)
                } else {
                    Result.failure(Exception("Ошибка сервера 1С (код $code)"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Получение расписания звонков колледжа.
     */
    suspend fun getBells(): Result<List<BellTime>> = withContext(Dispatchers.IO) {
        try {
            val trimmedUrl = config.serverUrl.trimEnd('/')
            val conn = createConnection("$trimmedUrl/bells")
            val code = conn.responseCode
            if (code in 200..299) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val bells = parseBellsJson(responseText)
                Result.success(bells)
            } else {
                Result.failure(Exception("Ошибка звонков 1С (код $code)"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseGroupsJson(jsonStr: String): List<StudentGroup> {
        val list = mutableListOf<StudentGroup>()
        val array = if (jsonStr.trim().startsWith("[")) JSONArray(jsonStr)
        else JSONObject(jsonStr).optJSONArray("groups") ?: JSONArray()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                StudentGroup(
                    id = obj.optString("id", obj.optString("name", "group_$i")),
                    name = obj.optString("name", "Группа $i"),
                    course = obj.optInt("course", 1),
                    department = obj.optString("department", "Колледж")
                )
            )
        }
        return list
    }

    private fun parseBellsJson(jsonStr: String): List<BellTime> {
        val list = mutableListOf<BellTime>()
        val array = if (jsonStr.trim().startsWith("[")) JSONArray(jsonStr)
        else JSONObject(jsonStr).optJSONArray("bells") ?: JSONArray()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                BellTime(
                    number = obj.optInt("number", i + 1),
                    timeStart = obj.optString("timeStart", "08:30"),
                    timeEnd = obj.optString("timeEnd", "10:00"),
                    breakMinutes = obj.optInt("breakMinutes", 10)
                )
            )
        }
        return list
    }

    private fun parseScheduleJson(jsonStr: String): List<DaySchedule> {
        val list = mutableListOf<DaySchedule>()
        val rootArray = if (jsonStr.trim().startsWith("[")) JSONArray(jsonStr)
        else JSONObject(jsonStr).optJSONArray("days") ?: JSONArray()

        for (i in 0 until rootArray.length()) {
            val dayObj = rootArray.getJSONObject(i)
            val lessonsArray = dayObj.optJSONArray("lessons") ?: JSONArray()
            val lessons = mutableListOf<Lesson>()

            for (j in 0 until lessonsArray.length()) {
                val lObj = lessonsArray.getJSONObject(j)
                val typeStr = lObj.optString("type", "PRACTICE").uppercase()
                val lessonType = when {
                    typeStr.contains("ЛЕКЦ") || typeStr.contains("LECTURE") -> LessonType.LECTURE
                    typeStr.contains("ЛАБ") || typeStr.contains("LAB") -> LessonType.LAB
                    typeStr.contains("ЗАЧ") || typeStr.contains("ЭКЗ") || typeStr.contains("EXAM") -> LessonType.EXAM
                    else -> LessonType.PRACTICE
                }

                val replacement = lObj.optString("replacementInfo", lObj.optString("replacement", ""))

                lessons.add(
                    Lesson(
                        id = lObj.optString("id", "les_${i}_$j"),
                        number = lObj.optInt("number", j + 1),
                        timeStart = lObj.optString("timeStart", "08:30"),
                        timeEnd = lObj.optString("timeEnd", "10:00"),
                        subject = lObj.optString("subject", "Предмет"),
                        teacher = lObj.optString("teacher", "Преподаватель"),
                        room = lObj.optString("room", "ауд. -"),
                        type = lessonType,
                        isCurrent = lObj.optBoolean("isCurrent", false),
                        isCanceled = lObj.optBoolean("isCanceled", false),
                        replacementInfo = if (replacement.isNotBlank()) replacement else null
                    )
                )
            }

            list.add(
                DaySchedule(
                    dayIndex = dayObj.optInt("dayIndex", i + 1),
                    dayShortName = dayObj.optString("dayShortName", getShortDay(i + 1)),
                    dayFullName = dayObj.optString("dayFullName", getFullDay(i + 1)),
                    dateText = dayObj.optString("dateText", ""),
                    isToday = dayObj.optBoolean("isToday", false),
                    lessons = lessons
                )
            )
        }
        return list
    }

    private fun getShortDay(index: Int): String = when (index) {
        1 -> "Пн"; 2 -> "Вт"; 3 -> "Ср"; 4 -> "Чт"; 5 -> "Пт"; 6 -> "Сб"; else -> "Вс"
    }

    private fun getFullDay(index: Int): String = when (index) {
        1 -> "Понедельник"; 2 -> "Вторник"; 3 -> "Среда"; 4 -> "Четверг"
        5 -> "Пятница"; 6 -> "Суббота"; else -> "Воскресенье"
    }
}
