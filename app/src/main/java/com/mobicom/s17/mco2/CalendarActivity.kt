package com.mobicom.s17.mco2

import android.graphics.Color
import android.os.Bundle
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CalendarActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var calendarGrid: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar)

        dbHelper = DatabaseHelper(this)
        calendarGrid = findViewById(R.id.prototypeCalendar)

        // Fetch moods by date
        val moodsByDate = dbHelper.getMoodsByDate()

        // Iterate through the GridLayout children (date cells)
        for (i in 0 until calendarGrid.childCount) {
            val dayView = calendarGrid.getChildAt(i) as TextView
            val day = dayView.text.toString()

            if (day.isNotEmpty()) {
                // Assume date format in DB matches "MM/DD/YYYY"
                val currentMonth = "06" // Change dynamically if needed
                val currentYear = "2025"
                val dateKey = "$currentMonth/$day/$currentYear"

                if (moodsByDate.containsKey(dateKey)) {
                    // Get mood and color cell accordingly
                    when (moodsByDate[dateKey]) {
                        "happy" -> dayView.setBackgroundColor(Color.parseColor("#FFD700")) // yellow
                        "sad" -> dayView.setBackgroundColor(Color.parseColor("#1E90FF")) // blue
                        "angry" -> dayView.setBackgroundColor(Color.parseColor("#FF4500")) // orange/red
                        "calm" -> dayView.setBackgroundColor(Color.parseColor("#90EE90")) // light green
                        else -> dayView.setBackgroundColor(Color.parseColor("#D3D3D3")) // default gray
                    }
                }
            }
        }
    }
}
