package com.mobicom.s17.mco2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import android.app.DatePickerDialog
import android.widget.EditText
import android.widget.Toast
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var frame: FrameLayout
    private lateinit var inflater: LayoutInflater
    private var currentUserEmail: String? = null  // Track the logged-in user

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        frame = findViewById(R.id.main_content_frame)
        inflater = LayoutInflater.from(this)

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

        // Open CalendarActivity instead of inflating XML
        btnCalendar.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
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
    }
}
