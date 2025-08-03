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
    private var currentUserEmail: String? = null

    // Calendar variables
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var calendarGrid: GridLayout
    private lateinit var headerText: TextView
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button
    private var currentMonth: Int = 0
    private var currentYear: Int = 0

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

        checkAndMigrateDatabase()

        val btnCalendar: ImageButton = findViewById(R.id.btn_calendar)
        val btnSummary: ImageButton = findViewById(R.id.btn_summary)
        val btnTimeline: ImageButton = findViewById(R.id.btn_timeline)
        val btnLogin: ImageButton = findViewById(R.id.btn_login)
        val btnPost: ImageButton = findViewById(R.id.btn_post)

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
            if (currentUserEmail != null) {
                switchPage(R.layout.profile)
            } else {
                switchPage(R.layout.activity_login)
            }
        }
    }

    private fun switchPage(layoutResId: Int) {
        frame.removeAllViews()
        val view = inflater.inflate(layoutResId, frame, false)
        frame.addView(view)

        val dbHelper = DatabaseHelper(this)

        // Login/Register
        if (layoutResId == R.layout.activity_login) {
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

        // Profile
        if (layoutResId == R.layout.profile && currentUserEmail != null) {
            val nameField = view.findViewById<EditText>(R.id.profileName)
            val birthdayField = view.findViewById<EditText>(R.id.profileBirthday)
            val ageField = view.findViewById<EditText>(R.id.profileAge)
            val emailField = view.findViewById<EditText>(R.id.profileEmail)
            val btnEditProfile = view.findViewById<Button>(R.id.btnEditProfile)
            val btnCancelEdit = view.findViewById<Button>(R.id.btnCancelEdit)

            val user = dbHelper.getUserByEmail(currentUserEmail!!)
            if (user != null) {
                nameField.setText(user.name)
                birthdayField.setText(user.birthday)
                emailField.setText(user.email)
                ageField.setText(dbHelper.calculateAge(user.birthday).toString())
            }

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
                if (user != null) {
                    nameField.setText(user.name)
                    birthdayField.setText(user.birthday)
                    ageField.setText(dbHelper.calculateAge(user.birthday).toString())
                }
                nameField.isEnabled = false
                birthdayField.isEnabled = false
                btnEditProfile.text = "Edit Profile"
                btnCancelEdit.visibility = View.GONE
            }
        }

        // Summary
        if (layoutResId == R.layout.summary) {
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

        // Calendar
        if (layoutResId == R.layout.calendar) {
            calendarGrid = view.findViewById(R.id.prototypeCalendar)
            headerText = view.findViewById(R.id.calendarHeader)
            prevButton = view.findViewById(R.id.btn_prev_month)
            nextButton = view.findViewById(R.id.btn_next_month)

            val cal = Calendar.getInstance()
            currentMonth = cal.get(Calendar.MONTH)
            currentYear = cal.get(Calendar.YEAR)

            prevButton.setOnClickListener {
                if (currentMonth == 0) {
                    currentMonth = 11; currentYear--
                } else currentMonth--
                updateCalendar()
            }
            nextButton.setOnClickListener {
                if (currentMonth == 11) {
                    currentMonth = 0; currentYear++
                } else currentMonth++
                updateCalendar()
            }
            updateCalendar()
        }

        // Timeline
        if (layoutResId == R.layout.timeline) {
            val container = view.findViewById<LinearLayout>(R.id.timelineContainer)
            val db = DatabaseHelper(this)
            val dbCursor = db.readableDatabase.rawQuery("SELECT * FROM moods ORDER BY id DESC", null)

            if (dbCursor.moveToFirst()) {
                do {
                    val id = dbCursor.getInt(dbCursor.getColumnIndexOrThrow("id"))
                    val mood = dbCursor.getString(dbCursor.getColumnIndexOrThrow("mood"))
                    val note = dbCursor.getString(dbCursor.getColumnIndexOrThrow("note"))
                    val date = dbCursor.getString(dbCursor.getColumnIndexOrThrow("date"))

                    val entryLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setBackgroundResource(R.drawable.timeline_entry_bg)
                        setPadding(12, 12, 12, 12)
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(8, 8, 8, 8)
                        layoutParams = params
                    }

                    val topRow = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    val iconView = ImageView(this).apply {
                        setImageResource(getMoodIcon(mood))
                        layoutParams = LinearLayout.LayoutParams(60.dp, 60.dp).apply {
                            setMargins(0, 0, 16, 0)
                        }
                    }
                    val textView = TextView(this).apply {
                        text = "${note.ifEmpty { "(No note)" }}\n$date"
                        setTextColor(Color.parseColor("#333333"))
                        textSize = 14f
                    }

                    val btnRow = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.END
                    }

                    val btnEdit = Button(this).apply {
                        text = "Edit"
                        setOnClickListener {
                            val editIntent = Intent(this@MainActivity, ActivityEnterText::class.java)
                            editIntent.putExtra("EDIT_MODE", true)
                            editIntent.putExtra("MOOD_ID", id)
                            startActivity(editIntent)
                        }
                    }

                    val btnDelete = Button(this).apply {
                        text = "Delete"
                        setOnClickListener {
                            dbHelper.deleteMood(id)
                            switchPage(R.layout.timeline)
                        }
                    }

                    btnRow.addView(btnEdit)
                    btnRow.addView(btnDelete)
                    topRow.addView(iconView)
                    topRow.addView(textView)

                    entryLayout.addView(topRow)
                    entryLayout.addView(btnRow)
                    container.addView(entryLayout)

                } while (dbCursor.moveToNext())
            } else {
                val emptyView = TextView(this).apply {
                    text = "No moods logged yet."
                    textSize = 16f
                    gravity = Gravity.CENTER
                }
                container.addView(emptyView)
            }
            dbCursor.close()
        }
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

        val totalCells = 42
        for (i in 0 until totalCells) {
            val dayNum = i - firstDayOfWeek + 1
            val dayView = TextView(this)
            val layoutParams = GridLayout.LayoutParams()
            layoutParams.width = 0
            layoutParams.height = 80.dp
            layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            dayView.layoutParams = layoutParams
            dayView.gravity = Gravity.CENTER
            dayView.textSize = 12f
            dayView.setTextColor(Color.parseColor("#6E2795"))
            dayView.setPadding(4, 4, 4, 4)

            if (dayNum in 1..daysInMonth) {
                val dateKey = "$monthStr/${String.format("%02d", dayNum)}/$yearStr"
                val moodEntry = moodsByDate[dateKey]
                val mood = moodEntry?.mood

                dayView.text = if (mood != null) "$dayNum\n$mood" else dayNum.toString()

                if (mood != null && moodColors.keys.any { it.equals(mood, ignoreCase = true) }) {
                    val colorKey = moodColors.keys.first { it.equals(mood, ignoreCase = true) }
                    dayView.setBackgroundColor(moodColors[colorKey]!!)

                    dayView.setOnClickListener {
                        AlertDialog.Builder(this)
                            .setTitle("Mood for $dateKey")
                            .setMessage("Mood: $mood")
                            .setPositiveButton("Edit") { _, _ ->
                                val editIntent = Intent(this, ActivityEnterText::class.java)
                                editIntent.putExtra("EDIT_MODE", true)
                                editIntent.putExtra("MOOD_ID", moodEntry!!.id)
                                startActivity(editIntent)
                            }
                            .setNegativeButton("Delete") { _, _ ->
                                dbHelper.deleteMood(moodEntry!!.id)
                                updateCalendar()
                            }
                            .setNeutralButton("Close", null)
                            .show()
                    }
                } else {
                    dayView.setBackgroundColor(Color.parseColor("#F0F0F0"))
                }
            } else {
                dayView.text = ""
                dayView.setBackgroundColor(Color.TRANSPARENT)
            }
            calendarGrid.addView(dayView)
        }
    }

    private fun checkAndMigrateDatabase() {
        val db = dbHelper.writableDatabase
        val cursor = db.rawQuery("PRAGMA table_info(moods)", null)
        var columnExists = false
        while (cursor.moveToNext()) {
            val columnName = cursor.getString(1)
            if (columnName == "extra_info") {
                columnExists = true
                break
            }
        }
        cursor.close()
        if (!columnExists) {
            db.execSQL("ALTER TABLE moods ADD COLUMN extra_info TEXT DEFAULT ''")
        }
    }

    private fun getMoodIcon(mood: String): Int {
        return when {
            mood.equals("upset", ignoreCase = true) -> R.drawable.upset
            mood.equals("down", ignoreCase = true) -> R.drawable.down
            mood.equals("neutral", ignoreCase = true) -> R.drawable.neutral
            mood.equals("coping", ignoreCase = true) -> R.drawable.coping
            mood.equals("elated", ignoreCase = true) -> R.drawable.elated
            else -> R.drawable.neutral
        }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
