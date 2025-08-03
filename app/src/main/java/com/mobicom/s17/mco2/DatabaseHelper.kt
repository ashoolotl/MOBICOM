package com.mobicom.s17.mco2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "VibeMate.db"
        private const val DATABASE_VERSION = 2

        // Users table
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USER_NAME = "name"
        const val COLUMN_USER_BIRTHDAY = "birthday"
        const val COLUMN_USER_EMAIL = "email"
        const val COLUMN_USER_PASSWORD = "password"

        // Moods table
        const val TABLE_MOODS = "moods"
        const val COLUMN_MOOD_ID = "id"
        const val COLUMN_MOOD = "mood"
        const val COLUMN_NOTE = "note"
        const val COLUMN_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NAME TEXT,
                $COLUMN_USER_BIRTHDAY TEXT,
                $COLUMN_USER_EMAIL TEXT UNIQUE,
                $COLUMN_USER_PASSWORD TEXT
            )"""
        )

        db.execSQL(
            """CREATE TABLE $TABLE_MOODS (
                $COLUMN_MOOD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_MOOD TEXT,
                $COLUMN_NOTE TEXT,
                $COLUMN_DATE TEXT
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_MOODS ADD COLUMN extra_info TEXT DEFAULT ''")
        }
    }

    // ---------------- USERS ----------------
    fun insertUser(name: String, birthday: String, email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, name)
            put(COLUMN_USER_BIRTHDAY, birthday)
            put(COLUMN_USER_EMAIL, email)
            put(COLUMN_USER_PASSWORD, password)
        }
        return db.insert(TABLE_USERS, null, values) > 0
    }

    fun authenticateUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL=? AND $COLUMN_USER_PASSWORD=?",
            arrayOf(email, password)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL=?",
            arrayOf(email)
        )
        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)),
                birthday = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_BIRTHDAY)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL))
            )
        }
        cursor.close()
        return user
    }

    fun updateUser(name: String, birthday: String, email: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, name)
            put(COLUMN_USER_BIRTHDAY, birthday)
        }
        return db.update(TABLE_USERS, values, "$COLUMN_USER_EMAIL=?", arrayOf(email)) > 0
    }

    fun calculateAge(birthday: String): Int {
        val parts = birthday.split("/")
        if (parts.size != 3) return 0
        val birthYear = parts[2].toIntOrNull() ?: return 0
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return currentYear - birthYear
    }

    // ---------------- MOODS ----------------
    fun insertMood(mood: String, note: String, date: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MOOD, mood)
            put(COLUMN_NOTE, note)
            put(COLUMN_DATE, date)
        }
        return db.insert(TABLE_MOODS, null, values) > 0
    }

    fun updateMood(id: Int, mood: String, note: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MOOD, mood)
            put(COLUMN_NOTE, note)
        }
        return db.update(TABLE_MOODS, values, "$COLUMN_MOOD_ID=?", arrayOf(id.toString())) > 0
    }

    fun deleteMood(id: Int): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_MOODS, "$COLUMN_MOOD_ID=?", arrayOf(id.toString())) > 0
    }

    fun getMoodsByDate(): Map<String, MoodEntry> {
        val db = readableDatabase
        val moods = mutableMapOf<String, MoodEntry>()
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MOODS", null)
        if (cursor.moveToFirst()) {
            do {
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                val mood = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD))
                val note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE))
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MOOD_ID))
                moods[date] = MoodEntry(id, mood, note)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return moods
    }

    fun getMoodCounts(): Map<String, Int> {
        val db = readableDatabase
        val moodCounts = mutableMapOf<String, Int>()
        val cursor = db.rawQuery(
            "SELECT $COLUMN_MOOD, COUNT(*) FROM $TABLE_MOODS GROUP BY $COLUMN_MOOD",
            null
        )
        if (cursor.moveToFirst()) {
            do {
                val mood = cursor.getString(0)
                val count = cursor.getInt(1)
                moodCounts[mood] = count
            } while (cursor.moveToNext())
        }
        cursor.close()
        return moodCounts
    }
}

// Models
data class User(val name: String, val birthday: String, val email: String)
data class MoodEntry(val id: Int, val mood: String, val note: String)
