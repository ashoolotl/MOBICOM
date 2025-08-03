package com.mobicom.s17.mco2

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class ActivityEnterText : AppCompatActivity() {

    private lateinit var btnUpset: ImageButton
    private lateinit var btnDown: ImageButton
    private lateinit var btnNeutral: ImageButton
    private lateinit var btnCoping: ImageButton
    private lateinit var btnElated: ImageButton
    private lateinit var inputNote: EditText
    private lateinit var btnSaveMood: Button
    private lateinit var dbHelper: DatabaseHelper

    private var selectedMood: String? = null
    private lateinit var moodButtons: List<ImageButton>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_text)

        dbHelper = DatabaseHelper(this)

        // Initialize views
        btnUpset = findViewById(R.id.btnUpset)
        btnDown = findViewById(R.id.btnDown)
        btnNeutral = findViewById(R.id.btnNeutral)
        btnCoping = findViewById(R.id.btnCoping)
        btnElated = findViewById(R.id.btnElated)
        inputNote = findViewById(R.id.inputNote)
        btnSaveMood = findViewById(R.id.btnSaveMood)

        moodButtons = listOf(btnUpset, btnDown, btnNeutral, btnCoping, btnElated)

        // Assign click listeners to mood buttons
        btnUpset.setOnClickListener { selectMood("Upset", btnUpset) }
        btnDown.setOnClickListener { selectMood("Down", btnDown) }
        btnNeutral.setOnClickListener { selectMood("Neutral", btnNeutral) }
        btnCoping.setOnClickListener { selectMood("Coping", btnCoping) }
        btnElated.setOnClickListener { selectMood("Elated", btnElated) }

        // Save button click listener
        btnSaveMood.setOnClickListener {
            if (selectedMood == null) {
                Toast.makeText(this, "Please select a mood first", Toast.LENGTH_SHORT).show()
            } else {
                val note = inputNote.text.toString()
                val date = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date())

                val success = dbHelper.insertMood(selectedMood!!, note, date)
                if (success) {
                    Toast.makeText(this, "Mood saved!", Toast.LENGTH_SHORT).show()

                    // Refresh Timeline automatically when returning
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("REFRESH_DATA", true)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to save mood", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun selectMood(mood: String, selectedButton: ImageButton) {
        selectedMood = mood

        // Highlight selected and reset others
        for (button in moodButtons) {
            if (button == selectedButton) {
                button.background = ContextCompat.getDrawable(this, R.drawable.mood_selector_bg)
                button.isSelected = true
            } else {
                button.background = null
                button.isSelected = false
            }
        }
    }
}
