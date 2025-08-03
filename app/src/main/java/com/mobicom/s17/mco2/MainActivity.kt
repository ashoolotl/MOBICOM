package com.mobicom.s17.mco2

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var frame: FrameLayout
    private lateinit var inflater: LayoutInflater
    private lateinit var dbHelper: DatabaseHelper
    private var currentUserEmail: String? = null

    // Calendar variables
    private lateinit var calendarGrid: GridLayout
    private lateinit var headerText: TextView
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button
    private var currentMonth = 0
    private var currentYear = 0

    // Summary variables
    private lateinit var upsetCount: TextView
    private lateinit var downCount: TextView
    private lateinit var neutralCount: TextView
    private lateinit var copingCount: TextView
    private lateinit var elatedCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        frame = findViewById(R.id.main_content_frame)
        inflater = LayoutInflater.from(this)
        dbHelper = DatabaseHelper(this)

        // DB schema check
        checkAndMigrateDatabase()

        val btnCalendar: ImageButton = findViewById(R.id.btn_calendar)
        val btnSummary: ImageButton = findViewById(R.id.btn_summary)
        val btnTimeline: ImageButton = findViewById(R.id.btn_timeline)
        val btnLogin: ImageButton = findViewById(R.id.btn_login)
        val btnPost: ImageButton = findViewById(R.id.btn_post)

        // Default page
        if (intent.getBooleanExtra("REFRESH_DATA", false)) {
            switchPage(R.layout.timeline)
        } else {
            switchPage(R.layout.activity_login)
        }

        btnPost.setOnClickListener {
            val intent = Intent(this, ActivityEnterText::class.java)
            startActivity(intent)
        }
        btnCalendar.setOnClickListener { switchPage(R.layout.calendar) }
        btnSummary.setOnClickListener { switchPage(R.layout.summary) }
        btnTimeline.setOnClickListener { switchPage(R.layout.timeline) }
        btnLogin.setOnClickListener {
            if (currentUserEmail != null) switchPage(R.layout.profile)
            else switchPage(R.layout.activity_login)
        }
    }

    private fun switchPage(layoutResId: Int) {
        frame.removeAllViews()
        val view = inflater.inflate(layoutResId, frame, false)
        frame.addView(view)

        when (layoutResId) {
            R.layout.activity_login -> setupLogin(view)
            R.layout.profile -> if (currentUserEmail != null) setupProfile(view)
            R.layout.summary -> setupSummary(view)
            R.layout.calendar -> setupCalendar(view)
            R.layout.timeline -> setupTimeline(view)
        }
    }

    // ----------------- LOGIN -------------------
    private fun setupLogin(view: View) {
        val loginLayout = view.findViewById<LinearLayout>(R.id.loginLayout)
        val registerLayout = view.findViewById<LinearLayout>(R.id.registerLayout)

        val btnLogin = view.findViewById<Button>(R.id.submitLoginButton)
        val btnShowRegister = view.findViewById<Button>(R.id.btnShowRegister)
        val btnRegister = view.findViewById<Button>(R.id.registerButton)
        val btnBackToLogin = view.findViewById<Button>(R.id.btnBackToLogin)

        val birthdayInput = view.findViewById<EditText>(R.id.regBirthdayInput)
        birthdayInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                birthdayInput.setText("${m + 1}/$d/$y")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.datePicker.maxDate = System.currentTimeMillis()
            datePicker.show()
        }

        btnShowRegister.setOnClickListener {
            loginLayout.visibility = View.GONE
            registerLayout.visibility = View.VISIBLE
        }

        btnBackToLogin.setOnClickListener {
            registerLayout.visibility = View.GONE
            loginLayout.visibility = View.VISIBLE
        }

        btnRegister.setOnClickListener {
            val name = view.findViewById<EditText>(R.id.regNameInput).text.toString()
            val birthday = view.findViewById<EditText>(R.id.regBirthdayInput).text.toString()
            val email = view.findViewById<EditText>(R.id.regEmailInput).text.toString()
            val password = view.findViewById<EditText>(R.id.regPasswordInput).text.toString()

            if (dbHelper.insertUser(name, birthday, email, password)) {
                currentUserEmail = email
                switchPage(R.layout.timeline)
            } else {
                Toast.makeText(this, "Error: Email already exists!", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogin.setOnClickListener {
            val email = view.findViewById<EditText>(R.id.loginEmailInput).text.toString()
            val password = view.findViewById<EditText>(R.id.loginPasswordInput).text.toString()

            if (dbHelper.authenticateUser(email, password)) {
                currentUserEmail = email
                switchPage(R.layout.timeline)
            } else {
                Toast.makeText(this, "Invalid login credentials!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ----------------- PROFILE -------------------
    private fun setupProfile(view: View) {
        val user = dbHelper.getUserByEmail(currentUserEmail!!) ?: return

        val nameField = view.findViewById<EditText>(R.id.profileName)
        val birthdayField = view.findViewById<EditText>(R.id.profileBirthday)
        val ageField = view.findViewById<EditText>(R.id.profileAge)
        val emailField = view.findViewById<EditText>(R.id.profileEmail)
        val btnEditProfile = view.findViewById<Button>(R.id.btnEditProfile)
        val btnCancelEdit = view.findViewById<Button>(R.id.btnCancelEdit)

        nameField.setText(user.name)
        birthdayField.setText(user.birthday)
        emailField.setText(user.email)
        ageField.setText(dbHelper.calculateAge(user.birthday).toString())

        birthdayField.setOnClickListener {
            if (birthdayField.isEnabled) {
                val calendar = Calendar.getInstance()
                val datePicker = DatePickerDialog(this, { _, y, m, d ->
                    val formattedDate = "${m + 1}/$d/$y"
                    birthdayField.setText(formattedDate)
                    ageField.setText(dbHelper.calculateAge(formattedDate).toString())
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                datePicker.datePicker.maxDate = System.currentTimeMillis()
                datePicker.show()
            }
        }

        btnEditProfile.setOnClickListener {
            if (btnEditProfile.text == "Edit Profile") {
                nameField.isEnabled = true
                birthdayField.isEnabled = true
                emailField.isEnabled = false
                btnEditProfile.text = "Save"
                btnCancelEdit.visibility = View.VISIBLE
            } else {
                val updatedName = nameField.text.toString()
                val updatedBirthday = birthdayField.text.toString()
                if (dbHelper.updateUser(updatedName, updatedBirthday, currentUserEmail!!)) {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                    nameField.isEnabled = false
                    birthdayField.isEnabled = false
                    btnEditProfile.text = "Edit Profile"
                    btnCancelEdit.visibility = View.GONE
                } else {
                    Toast.makeText(this, "Failed to update profile!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnCancelEdit.setOnClickListener {
            nameField.setText(user.name)
            birthdayField.setText(user.birthday)
            ageField.setText(dbHelper.calculateAge(user.birthday).toString())
            nameField.isEnabled = false
            birthdayField.isEnabled = false
            btnEditProfile.text = "Edit Profile"
            btnCancelEdit.visibility = View.GONE
        }
    }

    // ----------------- SUMMARY -------------------
    private fun setupSummary(view: View) {
        upsetCount = view.findViewById(R.id.upsetCount)
        downCount = view.findViewById(R.id.downCount)
        neutralCount = view.findViewById(R.id.neutralCount)
        copingCount = view.findViewById(R.id.copingCount)
        elatedCount = view.findViewById(R.id.elatedCount)

        val moodCounts = dbHelper.getMoodCounts()
        upsetCount.text = (moodCounts["Upset"] ?: 0).toString()
        downCount.text = (moodCounts["Down"] ?: 0).toString()
        neutralCount.text = (moodCounts["Neutral"] ?: 0).toString()
        copingCount.text = (moodCounts["Coping"] ?: 0).toString()
        elatedCount.text = (moodCounts["Elated"] ?: 0).toString()

        val barChart = view.findViewById<BarChart>(R.id.customBarChart)
        barChart.setData(moodCounts)
    }

    // ----------------- CALENDAR -------------------
    private fun setupCalendar(view: View) {
        calendarGrid = view.findViewById(R.id.prototypeCalendar)
        headerText = view.findViewById(R.id.calendarHeader)
        prevButton = view.findViewById(R.id.btn_prev_month)
        nextButton = view.findViewById(R.id.btn_next_month)

        val cal = Calendar.getInstance()
        currentMonth = cal.get(Calendar.MONTH)
        currentYear = cal.get(Calendar.YEAR)

        prevButton.setOnClickListener {
            if (currentMonth == 0) {
                currentMonth = 11
                currentYear--
            } else currentMonth--
            updateCalendar()
        }
        nextButton.setOnClickListener {
            if (currentMonth == 11) {
                currentMonth = 0
                currentYear++
            } else currentMonth++
            updateCalendar()
        }
        updateCalendar()
    }

    private fun updateCalendar() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, currentMonth)
        cal.set(Calendar.YEAR, currentYear)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        headerText.text = sdf.format(cal.time)

        calendarGrid.removeAllViews()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val moodsByDate = dbHelper.getMoodsByDate()
        val monthStr = String.format("%02d", currentMonth + 1)
        val yearStr = currentYear.toString()

        val moodColors = mapOf(
            "upset" to Color.GRAY,
            "down" to Color.BLUE,
            "neutral" to Color.GREEN,
            "coping" to Color.rgb(255, 165, 0),
            "elated" to Color.RED
        )

        for (i in 0 until 42) {
            val dayNum = i - firstDayOfWeek + 1
            val dayView = TextView(this)
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = 80.dp
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            dayView.layoutParams = params

            dayView.gravity = Gravity.CENTER
            dayView.textSize = 12f
            dayView.setTextColor(Color.parseColor("#6E2795"))
            dayView.setPadding(4, 4, 4, 4)

            if (dayNum in 1..daysInMonth) {
                val dateKey = "$monthStr/${String.format("%02d", dayNum)}/$yearStr"
                val mood = moodsByDate[dateKey]

                dayView.text = if (mood != null) "$dayNum\n$mood" else dayNum.toString()
                val moodKey = mood?.lowercase(Locale.getDefault())
                if (moodKey != null && moodColors.containsKey(moodKey)) {
                    dayView.setBackgroundColor(moodColors[moodKey]!!)
                } else dayView.setBackgroundColor(Color.parseColor("#F0F0F0"))

                dayView.setOnClickListener {
                    if (mood != null) showEditDialog(dateKey, mood, "")
                }
            } else {
                dayView.text = ""
                dayView.setBackgroundColor(Color.TRANSPARENT)
            }
            calendarGrid.addView(dayView)
        }
    }

    // ----------------- TIMELINE -------------------
    private fun setupTimeline(view: View) {
        val timelineContainer = view.findViewById<LinearLayout>(R.id.timelineContainer)
        timelineContainer.removeAllViews()

        val moodsByDate = dbHelper.getMoodsByDate()
        val sortedDates = moodsByDate.keys.sortedByDescending {
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).parse(it)
        }

        for (date in sortedDates) {
            val mood = moodsByDate[date]
            val textView = TextView(this)
            textView.text = "$date: $mood"
            textView.textSize = 16f
            textView.setPadding(10, 10, 10, 10)
            textView.setOnClickListener {
                if (mood != null) showEditDialog(date, mood, "")
            }
            timelineContainer.addView(textView)
        }
    }

    // ----------------- EDIT DIALOG -------------------
    private fun showEditDialog(date: String, currentMood: String, currentNote: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_mood, null)
        val editMood = dialogView.findViewById<EditText>(R.id.editMood)
        val editNote = dialogView.findViewById<EditText>(R.id.editNote)

        editMood.setText(currentMood)
        editNote.setText(currentNote)

        AlertDialog.Builder(this)
            .setTitle("Edit Mood Entry")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                if (dbHelper.updateMoodByDate(date, editMood.text.toString(), editNote.text.toString())) {
                    Toast.makeText(this, "Mood updated!", Toast.LENGTH_SHORT).show()
                    refreshAllPages()
                }
            }
            .setNegativeButton("Delete") { _, _ ->
                if (dbHelper.deleteMoodByDate(date)) {
                    Toast.makeText(this, "Mood deleted!", Toast.LENGTH_SHORT).show()
                    refreshAllPages()
                }
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun refreshAllPages() {
        switchPage(R.layout.summary)
    }

    // DB schema check
    private fun checkAndMigrateDatabase() {
        val db = dbHelper.writableDatabase
        val cursor = db.rawQuery("PRAGMA table_info(moods)", null)
        var columnExists = false
        while (cursor.moveToNext()) {
            if (cursor.getString(1) == "extra_info") {
                columnExists = true
                break
            }
        }
        cursor.close()
        if (!columnExists) {
            db.execSQL("ALTER TABLE moods ADD COLUMN extra_info TEXT DEFAULT ''")
        }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
