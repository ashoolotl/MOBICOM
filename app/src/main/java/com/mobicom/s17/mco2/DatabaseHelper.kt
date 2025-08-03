package com.mobicom.s17.mco2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "VibeMate.db"
        private const val DATABASE_VERSION = 1

        // Table name and columns
        private const val TABLE_USERS = "users"
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_BIRTHDAY = "birthday"
        private const val COL_EMAIL = "email"
        private const val COL_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = ("CREATE TABLE $TABLE_USERS ("
                + "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_NAME TEXT, "
                + "$COL_BIRTHDAY TEXT, "
                + "$COL_EMAIL TEXT UNIQUE, "
                + "$COL_PASSWORD TEXT)")
        db.execSQL(createUsersTable)

        // Create moods table
        val createMoodsTable = ("CREATE TABLE moods ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "mood TEXT, "
                + "note TEXT, "
                + "date TEXT)")
        db.execSQL(createMoodsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS moods") // Drop moods table as well
        onCreate(db)
    }

    // Insert user into DB
    fun insertUser(name: String, birthday: String, email: String, password: String): Boolean {
        val db = writableDatabase

        // Check if email already exists
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COL_EMAIL = ?", arrayOf(email))
        if (cursor.count > 0) {
            cursor.close()
            return false
        }
        cursor.close()

        val values = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_BIRTHDAY, birthday)
            put(COL_EMAIL, email)
            put(COL_PASSWORD, password)
        }

        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    // Authenticate user login
    fun authenticateUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COL_EMAIL = ? AND $COL_PASSWORD = ?",
            arrayOf(email, password)
        )

        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Fetch user details by email
    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COL_EMAIL = ?", arrayOf(email))

        var user: User? = null
        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME))
            val birthday = cursor.getString(cursor.getColumnIndexOrThrow(COL_BIRTHDAY))
            val userEmail = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL))

            user = User(name, birthday, userEmail)
        }
        cursor.close()
        return user
    }

    // Update user details
    fun updateUser(name: String, birthday: String, email: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_BIRTHDAY, birthday)
        }
        val result = db.update(TABLE_USERS, values, "$COL_EMAIL = ?", arrayOf(email))
        return result > 0
    }

    // Calculate age based on birthday (MM/DD/YYYY)
    fun calculateAge(birthday: String): Int {
        return try {
            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val dob = sdf.parse(birthday)
            val today = Calendar.getInstance()

            val dobCal = Calendar.getInstance()
            dobCal.time = dob!!

            var age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR)

            if (today.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (e: Exception) {
            0
        }
    }

    // Count moods grouped by mood type
    fun getMoodCounts(): Map<String, Int> {
        val db = this.readableDatabase
        val moodCounts = mutableMapOf<String, Int>()

        val cursor = db.rawQuery(
            "SELECT mood, COUNT(*) as count FROM moods GROUP BY mood", null
        )

        if (cursor.moveToFirst()) {
            do {
                val mood = cursor.getString(cursor.getColumnIndexOrThrow("mood"))
                val count = cursor.getInt(cursor.getColumnIndexOrThrow("count"))
                moodCounts[mood] = count
            } while (cursor.moveToNext())
        }
        cursor.close()
        return moodCounts
    }

    // Insert mood entry into DB
    fun insertMood(mood: String, note: String, date: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("mood", mood)
            put("note", note)
            put("date", date)
        }
        val result = db.insert("moods", null, values)
        return result != -1L
    }

    // ✅ Fetch moods by date for calendar coloring
    fun getMoodsByDate(): Map<String, String> {
        val db = readableDatabase
        val moodsByDate = mutableMapOf<String, String>()

        val cursor = db.rawQuery("SELECT date, mood FROM moods", null)
        if (cursor.moveToFirst()) {
            do {
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                val mood = cursor.getString(cursor.getColumnIndexOrThrow("mood"))

                // Store the mood for that date (if multiple moods exist, last one will overwrite)
                moodsByDate[date] = mood
            } while (cursor.moveToNext())
        }
        cursor.close()

        return moodsByDate
    }
}

// Data class for User details
data class User(
    val name: String,
    val birthday: String,
    val email: String
)
