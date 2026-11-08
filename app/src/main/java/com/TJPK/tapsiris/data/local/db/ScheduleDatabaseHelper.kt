package com.TJPK.tapsiris.data.local.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.TJPK.tapsiris.data.mock.MockScheduleData
import com.TJPK.tapsiris.domain.model.StudentGroup

/**
 * =========================================================================================
 * СЛОЙ DATA / LOCAL / DB: Локальная реляционная база данных SQLite для приложения «Тапсырыс».
 * Обеспечивает автономное хранение учебных групп и синхронизацию с 1С-базой.
 * =========================================================================================
 */
class ScheduleDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        const val DATABASE_NAME = "tapsiris_schedule.db"
        const val DATABASE_VERSION = 1

        const val TABLE_GROUPS = "student_groups"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_COURSE = "course"
        const val COLUMN_DEPARTMENT = "department"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createGroupsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_GROUPS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_COURSE INTEGER NOT NULL,
                $COLUMN_DEPARTMENT TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createGroupsTable)

        // Первичное наполнение базы данных группами колледжа
        populateDefaultGroups(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GROUPS")
        onCreate(db)
    }

    private fun populateDefaultGroups(db: SQLiteDatabase) {
        MockScheduleData.groups.forEach { group ->
            val values = ContentValues().apply {
                put(COLUMN_ID, group.id)
                put(COLUMN_NAME, group.name)
                put(COLUMN_COURSE, group.course)
                put(COLUMN_DEPARTMENT, group.department)
            }
            db.insertWithOnConflict(TABLE_GROUPS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    /**
     * Получить все учебные группы из локальной базы данных.
     */
    fun getAllGroups(): List<StudentGroup> {
        val list = mutableListOf<StudentGroup>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_GROUPS,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_COURSE, COLUMN_DEPARTMENT),
            null,
            null,
            null,
            null,
            "$COLUMN_COURSE ASC, $COLUMN_NAME ASC"
        )
        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(COLUMN_ID)
            val nameIndex = it.getColumnIndexOrThrow(COLUMN_NAME)
            val courseIndex = it.getColumnIndexOrThrow(COLUMN_COURSE)
            val deptIndex = it.getColumnIndexOrThrow(COLUMN_DEPARTMENT)

            while (it.moveToNext()) {
                list.add(
                    StudentGroup(
                        id = it.getString(idIndex),
                        name = it.getString(nameIndex),
                        course = it.getInt(courseIndex),
                        department = it.getString(deptIndex)
                    )
                )
            }
        }
        return list
    }

    /**
     * Сохранить или обновить группы в базе данных.
     * @param clearOld Если true, очищает устаревшие моковые данные перед записью реальных групп из 1С.
     */
    fun saveGroups(groups: List<StudentGroup>, clearOld: Boolean = false) {
        if (groups.isEmpty()) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            if (clearOld) {
                db.delete(TABLE_GROUPS, null, null)
            }
            for (group in groups) {
                val values = ContentValues().apply {
                    put(COLUMN_ID, group.id)
                    put(COLUMN_NAME, group.name)
                    put(COLUMN_COURSE, group.course)
                    put(COLUMN_DEPARTMENT, group.department)
                }
                db.insertWithOnConflict(TABLE_GROUPS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Поиск группы по названию в локальной базе данных.
     */
    fun findGroupByName(name: String): StudentGroup? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_GROUPS,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_COURSE, COLUMN_DEPARTMENT),
            "$COLUMN_NAME = ? COLLATE NOCASE",
            arrayOf(name.trim()),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return StudentGroup(
                    id = it.getString(it.getColumnIndexOrThrow(COLUMN_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                    course = it.getInt(it.getColumnIndexOrThrow(COLUMN_COURSE)),
                    department = it.getString(it.getColumnIndexOrThrow(COLUMN_DEPARTMENT))
                )
            }
        }
        return null
    }
}
