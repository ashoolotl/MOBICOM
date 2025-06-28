package com.mobicom.s17.mco2

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageButton
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

        // Load default page on startup
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
    }
}
