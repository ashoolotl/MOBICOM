package com.mobicom.s17.mco2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var calendarGrid: GridLayout
    private lateinit var headerText: TextView
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button
    private var currentMonth: Int = 0
    private var currentYear: Int = 0
    private lateinit var moodsByDate: Map<String, MoodEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar)

        dbHelper = DatabaseHelper(this)
        calendarGrid = findViewById(R.id.prototypeCalendar)
        headerText = findViewById(R.id.calendarHeader)

        // Add navigation buttons programmatically
        val navLayout = (headerText.parent as ViewGroup)
        prevButton = Button(this).apply { text = "<" }
        nextButton = Button(this).apply { text = ">" }
        navLayout.addView(prevButton, 0)
        navLayout.addView(nextButton)

        val cal = Calendar.getInstance()
        currentMonth = cal.get(Calendar.MONTH)
        currentYear = cal.get(Calendar.YEAR)

        prevButton.setOnClickListener {
            if (currentMonth == 0) {
                currentMonth = 11
                currentYear--
            } else {
                currentMonth--
            }
            updateCalendar()
        }
        nextButton.setOnClickListener {
            if (currentMonth == 11) {
                currentMonth = 0
                currentYear++
            } else {
                currentMonth++
            }
            updateCalendar()
        }

        // Setup bottom navbar just like other screens
        setupBottomNav()

        updateCalendar()
    }

    private fun updateCalendar() {
        // Update header
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, currentMonth)
        cal.set(Calendar.YEAR, currentYear)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        headerText.text = sdf.format(cal.time)

        // Clear previous views
        calendarGrid.removeAllViews()

        // Load moods from DB
        moodsByDate = dbHelper.getMoodsByDate()

        // Get first day of month and number of days
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Fill grid
        val totalCells = 42 // 7x6
        for (i in 0 until totalCells) {
            val dayNum = i - firstDayOfWeek + 1
            val dayView = TextView(this)

            // Use GridLayout.LayoutParams to properly distribute cells
            val layoutParams = GridLayout.LayoutParams()
            layoutParams.width = 0
            layoutParams.height = 48.dp
            layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            dayView.layoutParams = layoutParams

            dayView.gravity = Gravity.CENTER
            dayView.textSize = 16f
            dayView.setTextColor(Color.parseColor("#6E2795"))
            dayView.setBackgroundResource(R.drawable.bg_day_cell)

            if (dayNum in 1..daysInMonth) {
                dayView.text = dayNum.toString()
                val dateKey = String.format("%02d/%02d/%04d", currentMonth + 1, dayNum, currentYear)

                if (moodsByDate.containsKey(dateKey)) {
                    // Show mood color
                    val moodEntry = moodsByDate[dateKey]
                    dayView.setBackgroundColor(getMoodColor(moodEntry?.mood ?: ""))

                    // Add click listener to edit/delete
                    dayView.setOnClickListener {
                        moodEntry?.let { entry ->
                            showEditDeleteDialog(entry)
                        }
                    }
                } else {
                    // No entry for this date
                    dayView.setOnClickListener {
                        Toast.makeText(this, "No entry for this date", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                dayView.text = ""
                dayView.setBackgroundColor(Color.TRANSPARENT)
            }
            calendarGrid.addView(dayView)
        }
    }

    private fun showEditDeleteDialog(entry: MoodEntry) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setTitle("Manage Entry")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editMoodEntry(entry)
                    1 -> deleteMoodEntry(entry)
                }
            }
            .show()
    }

    private fun editMoodEntry(entry: MoodEntry) {
        val intent = Intent(this, ActivityEnterText::class.java)
        intent.putExtra("MOOD_ID", entry.id)
        intent.putExtra("MOOD", entry.mood)
        intent.putExtra("NOTE", entry.note)
        startActivity(intent)
    }

    private fun deleteMoodEntry(entry: MoodEntry) {
        val deleted = dbHelper.deleteMood(entry.id)
        if (deleted) {
            Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()
            updateCalendar()
        } else {
            Toast.makeText(this, "Failed to delete entry", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMoodColor(mood: String): Int {
        return when (mood.lowercase()) {
            "happy", "elated" -> Color.parseColor("#FFD700")
            "sad", "down" -> Color.parseColor("#1E90FF")
            "angry", "upset" -> Color.parseColor("#FF4500")
            "calm", "neutral", "coping" -> Color.parseColor("#90EE90")
            else -> Color.parseColor("#D3D3D3")
        }
    }

    private fun setupBottomNav() {
        val btnPost = findViewById<ImageButton>(R.id.btn_post)
        val btnCalendar = findViewById<ImageButton>(R.id.btn_calendar)
        val btnSummary = findViewById<ImageButton>(R.id.btn_summary)
        val btnTimeline = findViewById<ImageButton>(R.id.btn_timeline)
        val btnLogin = findViewById<ImageButton>(R.id.btn_login)

        btnPost.setOnClickListener {
            startActivity(Intent(this, ActivityEnterText::class.java))
            finish()
        }

        btnCalendar.setOnClickListener {
            Toast.makeText(this, "You're already on this screen!", Toast.LENGTH_SHORT).show()
        }

        btnSummary.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnTimeline.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // Extension property for dp to px
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
