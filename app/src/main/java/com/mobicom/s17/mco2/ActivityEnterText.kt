package com.mobicom.s17.mco2

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class ActivityEnterText : AppCompatActivity() {

    private var selectedMood: String? = null
    private lateinit var dbHelper: DatabaseHelper
    private var editId: Int? = null // To check if editing existing mood

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_text)

        dbHelper = DatabaseHelper(this)

        val moodUpset = findViewById<ImageView>(R.id.mood_upset)
        val moodDown = findViewById<ImageView>(R.id.mood_down)
        val moodNeutral = findViewById<ImageView>(R.id.mood_neutral)
        val moodCoping = findViewById<ImageView>(R.id.mood_coping)
        val moodElated = findViewById<ImageView>(R.id.mood_elated)
        val noteInput = findViewById<EditText>(R.id.noteInput)
        val saveButton = findViewById<Button>(R.id.saveButton)

        // Nav buttons
        val btnPost = findViewById<ImageButton>(R.id.btn_post)
        val btnCalendar = findViewById<ImageButton>(R.id.btn_calendar)
        val btnSummary = findViewById<ImageButton>(R.id.btn_summary)
        val btnTimeline = findViewById<ImageButton>(R.id.btn_timeline)
        val btnLogin = findViewById<ImageButton>(R.id.btn_login)

        val moods = mapOf(
            moodUpset to "Upset",
            moodDown to "Down",
            moodNeutral to "Neutral",
            moodCoping to "Coping",
            moodElated to "Elated"
        )

        // --- EDIT MODE CHECK ---
        editId = intent.getIntExtra("EDIT_ID", -1).takeIf { it != -1 }
        val existingMood = intent.getStringExtra("MOOD")
        val existingNote = intent.getStringExtra("NOTE")

        if (editId != null) {
            selectedMood = existingMood
            noteInput.setText(existingNote)
            moods.forEach { (view, mood) ->
                view.isSelected = mood.equals(existingMood, ignoreCase = true)
            }
            saveButton.text = "Update Mood"
        }

        // Mood selection logic
        moods.forEach { (view, mood) ->
            view.setOnClickListener {
                moods.keys.forEach { it.isSelected = false }
                view.isSelected = true
                selectedMood = mood
            }
        }

        // Save/Update button logic
        saveButton.setOnClickListener {
            if (selectedMood == null) {
                Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val note = noteInput.text.toString()
            val date = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date())

            if (editId != null) {
                val updated = dbHelper.updateMood(editId!!, selectedMood!!, note)
                Toast.makeText(this, if (updated) "Mood updated!" else "Error updating mood!", Toast.LENGTH_SHORT).show()
            } else {
                val inserted = dbHelper.insertMood(selectedMood!!, note, date)
                Toast.makeText(this, if (inserted) "Mood saved!" else "Error saving mood!", Toast.LENGTH_SHORT).show()
            }

            setResult(RESULT_OK)
            finish()
        }

        // Navbar button listeners -> just return to MainActivity
        btnPost.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        btnCalendar.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        btnSummary.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        btnTimeline.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        btnLogin.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }
}
