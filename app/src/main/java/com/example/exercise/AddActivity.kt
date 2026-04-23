package com.example.exercise

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.exercise.controller.DiaryController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddActivity : AppCompatActivity() {
    private lateinit var activity : EditText
    private lateinit var sets : EditText
    private lateinit var reps : EditText
    private lateinit var weight : EditText
    private lateinit var highRB : RadioButton
    private lateinit var mediumRB : RadioButton
    private lateinit var lowRB : RadioButton
    private lateinit var submitBtn : Button
    private lateinit var cancelBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate( savedInstanceState )

        setContentView(R.layout.activity_add)

        // get view elements
        activity = findViewById<EditText>( R.id.activity_input )
        sets = findViewById<EditText>( R.id.sets_input )
        reps = findViewById<EditText>( R.id.reps_input )
        weight = findViewById<EditText>( R.id.weight_input )
        highRB = findViewById<RadioButton>( R.id.high )
        mediumRB = findViewById<RadioButton>( R.id.medium )
        lowRB = findViewById<RadioButton>( R.id.low )
        submitBtn = findViewById<Button>( R.id.submit )
        cancelBtn = findViewById<Button>( R.id.cancel )

        // set button listeners
        submitBtn.setOnClickListener{ submit() }
        cancelBtn.setOnClickListener{ finish() }
    }

    fun submit() {
        val diary : DiaryController = MainActivity.diary
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = formatter.format(Date())
        val activity = activity.text.toString()
        val sets = sets.toString()
        val reps = reps.toString()
        val weight = weight.toString()
        var intensity : String = ""
        var validInput : Boolean = true

        // get intensity level
        if( highRB.isChecked ) {
            intensity = "High"
        } else if ( mediumRB.isChecked ) {
            intensity = "Medium"
        } else if ( lowRB.isChecked ) {
            intensity = "Low"
        } else {
            validInput = false
        }

        // validate all text fields are completed
        if (activity == "" || sets == "" || reps == "" || weight == "") {
            validInput = false
        }

        if (validInput) {
            diary.addWorkout(
                date = currentDate,
                activity = activity,
                sets = sets.toInt(),
                reps = reps.toInt(),
                weight = weight.toDouble(),
                intensity = intensity
            )

            finish()
        } else {
            Toast.makeText(this, "Please complete all fields.", Toast.LENGTH_LONG).show()
        }
    }
}