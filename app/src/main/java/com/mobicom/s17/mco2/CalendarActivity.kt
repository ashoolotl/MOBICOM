package com.mobicom.s17.mco2

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar)

        dbHelper = DatabaseHelper(this)
        calendarGrid = findViewById(R.id.prototypeCalendar)
        headerText = findViewById(R.id.calendarHeader)

        // Add navigation buttons programmatically
        val navLayout = (headerText.parent as ViewGroup) as ViewGroup
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

        // Get first day of month and number of days
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Fetch moods by date
        val moodsByDate = dbHelper.getMoodsByDate()
        val monthStr = String.format("%02d", currentMonth + 1)
        val yearStr = currentYear.toString()

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
                val dateKey = "$monthStr/${String.format("%02d", dayNum)}/$yearStr"
                when (moodsByDate[dateKey]) {
                    "happy" -> dayView.setBackgroundColor(Color.parseColor("#FFD700"))
                    "sad" -> dayView.setBackgroundColor(Color.parseColor("#1E90FF"))
                    "angry" -> dayView.setBackgroundColor(Color.parseColor("#FF4500"))
                    "calm" -> dayView.setBackgroundColor(Color.parseColor("#90EE90"))
                    else -> dayView.setBackgroundColor(Color.parseColor("#D3D3D3"))
                }
            } else {
                dayView.text = ""
                dayView.setBackgroundColor(Color.TRANSPARENT)
            }
            calendarGrid.addView(dayView)
        }
    }

    // Extension property for dp to px
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
