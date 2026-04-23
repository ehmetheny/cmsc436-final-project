package com.example.exercise

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Button
import android.widget.CalendarView
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import com.example.exercise.controller.DiaryController
import com.example.exercise.model.Exercise
import com.example.exercise.repository.FirebaseRepository

class MainActivity : AppCompatActivity() {
    private lateinit var calendarView : CalendarView
    private lateinit var sessionStatus : TextView
    private lateinit var statusCircle : Button
    private lateinit var addWorkoutBtn : Button
    private lateinit var trackCardioBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Initialize Repository and Controller
        val firebaseRepository = FirebaseRepository()
        diary = DiaryController( firebaseRepository )

        // Minimal Test: Add a workout
        // testBackend()

        calendarView = findViewById<CalendarView>(R.id.calendar)
        sessionStatus = findViewById<TextView>( R.id.session_status )
        statusCircle = findViewById<Button>( R.id.status_circle )
        addWorkoutBtn = findViewById<Button>( R.id.add_workout )
        trackCardioBtn = findViewById<Button>( R.id.track_cardio )

        addWorkoutBtn.setOnClickListener{ addWorkout() }
        trackCardioBtn.setOnClickListener{ trackCardio() }

        calendarView.setOnDateChangeListener { _, year, month, day ->
            val date = if (month + 1 < 10) {
                "$year-0${month + 1}-$day"
            } else {
                "$year-${month + 1}-$day"
            }
            openLog( date )
        }

        updateSessionStatus()
    }

    private fun testBackend() {
        Log.d("ExerciseApp", "Starting Backend Test...")

        // 1. Add a sample workout
        diary.addWorkout(
            date = "2023-10-27",
            activity = "Bench Press",
            sets = 3,
            reps = 10,
            weight = 60.0,
            intensity = "High"
        )
        Log.d("ExerciseApp", "Workout added attempt made.")

        // 2. Fetch workouts and log them
        diary.fetchWorkouts { diaryEntries ->
            Log.d("ExerciseApp", "Fetched ${diaryEntries.size} workouts:")

            diaryEntries.forEach { exercise ->
                when (exercise) {
                    is Exercise.Workout ->
                        Log.d("ExerciseApp", " - ${exercise.activity} on " +
                                "${exercise.date}: ${exercise.sets}x${exercise.reps} @ " +
                                "${exercise.weight}kg (ID: ${exercise.id})")
                    is Exercise.Cardio ->
                        Log.d("ExerciseApp", " - ${exercise.duration} minute " +
                                "cardio session across ${exercise.distance} miles on" +
                                " ${exercise.date} (ID: ${exercise.id})")
                }
            }
        }
    }

    fun openLog(date : String) {
        val intent : Intent = Intent( this, LogActivity::class.java )
        intent.putExtra("date", date)
        startActivity( intent )
    }


    fun addWorkout() {
        val intent : Intent = Intent( this, AddActivity::class.java )
        startActivity( intent )
    }

    fun trackCardio() {
        if (diary.getCardioSessionStatus()) {
            diary.stopCardioSession()
        } else {
            diary.startCardioSession()
        }
        updateSessionStatus()
    }

    fun updateSessionStatus() {
        if (diary.getCardioSessionStatus()) {
            sessionStatus.text = "cardio session IN PROGRESS"
            statusCircle.setBackgroundColor(Color.parseColor("#1e792c"))
        } else {
            sessionStatus.text = "cardio session NOT in progress"
            statusCircle.setBackgroundColor(Color.parseColor("#b80f0a"))
        }
    }

    companion object {
        lateinit var diary : DiaryController
    }
}
