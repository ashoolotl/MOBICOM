package com.mobicom.s17.mco2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.app.DatePickerDialog
import android.widget.EditText
import android.widget.Toast
import android.graphics.Color
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var frame: FrameLayout
    private lateinit var inflater: LayoutInflater
    private var currentUserEmail: String? = null  // Track the logged-in user

    // Calendar-related variables
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var calendarGrid: GridLayout
    private lateinit var headerText: TextView
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button
    private var currentMonth: Int = 0
    private var currentYear: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        frame = findViewById(R.id.main_content_frame)
        inflater = LayoutInflater.from(this)
        dbHelper = DatabaseHelper(this)

        val btnCalendar: ImageButton = findViewById(R.id.btn_calendar)
        val btnSummary: ImageButton = findViewById(R.id.btn_summary)
        val btnTimeline: ImageButton = findViewById(R.id.btn_timeline)
        val btnLogin: ImageButton = findViewById(R.id.btn_login)
        val btnPost: ImageButton = findViewById(R.id.btn_post)

        // Load startup page as default
        switchPage(R.layout.activity_login)

        btnPost.setOnClickListener {
            switchPage(R.layout.activity_enter_text)
        }

        // Switch to calendar view in the main frame
        btnCalendar.setOnClickListener {
            switchPage(R.layout.calendar)
        }

        btnSummary.setOnClickListener {
            switchPage(R.layout.summary)
        }

        btnTimeline.setOnClickListener {
            switchPage(R.layout.timeline)
        }

        btnLogin.setOnClickListener {
            // If logged in, go to profile, else show login page
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

        // Login/Register page
        if (layoutResId == R.layout.activity_login) {
            val loginLayout = view.findViewById<LinearLayout>(R.id.loginLayout)
            val registerLayout = view.findViewById<LinearLayout>(R.id.registerLayout)

            val btnLogin = view.findViewById<Button>(R.id.submitLoginButton)
            val btnShowRegister = view.findViewById<Button>(R.id.btnShowRegister)
            val btnRegister = view.findViewById<Button>(R.id.registerButton)
            val btnBackToLogin = view.findViewById<Button>(R.id.btnBackToLogin)

            // Birthday date picker
            val birthdayInput = view.findViewById<EditText>(R.id.regBirthdayInput)
            birthdayInput.setOnClickListener {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePicker = DatePickerDialog(this, { _, y, m, d ->
                    val formattedDate = "${m + 1}/$d/$y"
                    birthdayInput.setText(formattedDate)
                }, year, month, day)
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

        // Profile page
        if (layoutResId == R.layout.profile && currentUserEmail != null) {
            val nameField = view.findViewById<EditText>(R.id.profileName)
            val birthdayField = view.findViewById<EditText>(R.id.profileBirthday)
            val ageField = view.findViewById<EditText>(R.id.profileAge)
            val emailField = view.findViewById<EditText>(R.id.profileEmail)
            val btnEditProfile = view.findViewById<Button>(R.id.btnEditProfile)
            val btnCancelEdit = view.findViewById<Button>(R.id.btnCancelEdit)

            // Load user data
            val user = dbHelper.getUserByEmail(currentUserEmail!!)
            if (user != null) {
                nameField.setText(user.name)
                birthdayField.setText(user.birthday)
                emailField.setText(user.email)

                val age = dbHelper.calculateAge(user.birthday)
                ageField.setText(age.toString())
            }

            // Birthday DatePicker when editing
            birthdayField.setOnClickListener {
                if (birthdayField.isEnabled) {
                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    val datePicker = DatePickerDialog(this, { _, y, m, d ->
                        val formattedDate = "${m + 1}/$d/$y"
                        birthdayField.setText(formattedDate)
                        ageField.setText(dbHelper.calculateAge(formattedDate).toString())
                    }, year, month, day)
                    datePicker.datePicker.maxDate = System.currentTimeMillis()
                    datePicker.show()
                }
            }

            // Edit/Save functionality
            btnEditProfile.setOnClickListener {
                if (btnEditProfile.text == "Edit Profile") {
                    // Enable editing
                    nameField.isEnabled = true
                    birthdayField.isEnabled = true
                    emailField.isEnabled = false // Email stays locked
                    btnEditProfile.text = "Save"
                    btnCancelEdit.visibility = View.VISIBLE
                } else {
                    // Save changes
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

            // Cancel button
            btnCancelEdit.setOnClickListener {
                if (user != null) {
                    nameField.setText(user.name)
                    birthdayField.setText(user.birthday)
                    val age = dbHelper.calculateAge(user.birthday)
                    ageField.setText(age.toString())
                }

                nameField.isEnabled = false
                birthdayField.isEnabled = false
                btnEditProfile.text = "Edit Profile"
                btnCancelEdit.visibility = View.GONE
            }
        }

        // Calendar page
        if (layoutResId == R.layout.calendar) {
            calendarGrid = view.findViewById(R.id.prototypeCalendar)
            headerText = view.findViewById(R.id.calendarHeader)
            prevButton = view.findViewById(R.id.btn_prev_month)
            nextButton = view.findViewById(R.id.btn_next_month)

            // Initialize calendar to current month
            val cal = Calendar.getInstance()
            currentMonth = cal.get(Calendar.MONTH)
            currentYear = cal.get(Calendar.YEAR)

            // Set up navigation buttons
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
            layoutParams.height = 80.dp // Increased height to accommodate mood text
            layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            dayView.layoutParams = layoutParams

            dayView.gravity = Gravity.CENTER
            dayView.textSize = 12f // Smaller text to fit both day and mood
            dayView.setTextColor(Color.parseColor("#6E2795"))
            dayView.setPadding(4, 4, 4, 4)

            if (dayNum in 1..daysInMonth) {
                val dateKey = "$monthStr/${String.format("%02d", dayNum)}/$yearStr"
                val mood = moodsByDate[dateKey]

                // Set text to show both day number and mood (if any)
                dayView.text = if (mood != null) {
                    "$dayNum\n$mood"
                } else {
                    dayNum.toString()
                }

                // Set background color based on mood
                when (mood) {
                    "happy" -> dayView.setBackgroundColor(Color.parseColor("#FFD700"))
                    "sad" -> dayView.setBackgroundColor(Color.parseColor("#1E90FF"))
                    "angry" -> dayView.setBackgroundColor(Color.parseColor("#FF4500"))
                    "calm" -> dayView.setBackgroundColor(Color.parseColor("#90EE90"))
                    else -> dayView.setBackgroundColor(Color.parseColor("#F0F0F0"))
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
