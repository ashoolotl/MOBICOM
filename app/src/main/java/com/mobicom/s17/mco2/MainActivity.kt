package com.mobicom.s17.mco2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var frame: FrameLayout
    private lateinit var inflater: LayoutInflater

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
            switchPage(R.layout.activity_login)
        }
    }

    private fun switchPage(layoutResId: Int) {
        frame.removeAllViews()
        val view = inflater.inflate(layoutResId, frame, false)
        frame.addView(view)

        if (layoutResId == R.layout.activity_login) {
            // Toggle between login and register forms
            val loginLayout = view.findViewById<LinearLayout>(R.id.loginLayout)
            val registerLayout = view.findViewById<LinearLayout>(R.id.registerLayout)

            val btnLogin = view.findViewById<Button>(R.id.submitLoginButton)
            val btnShowRegister = view.findViewById<Button>(R.id.btnShowRegister)
            val btnRegister = view.findViewById<Button>(R.id.registerButton)
            val btnBackToLogin = view.findViewById<Button>(R.id.btnBackToLogin)

            // Login button -> Timeline
            btnLogin.setOnClickListener {
                switchPage(R.layout.timeline)
            }

            // Show Register form
            btnShowRegister.setOnClickListener {
                loginLayout.visibility = View.GONE
                registerLayout.visibility = View.VISIBLE
            }

            // Back to login form
            btnBackToLogin.setOnClickListener {
                registerLayout.visibility = View.GONE
                loginLayout.visibility = View.VISIBLE
            }

            // Register button -> Timeline
            btnRegister.setOnClickListener {
                switchPage(R.layout.timeline)
            }
        }
    }
}