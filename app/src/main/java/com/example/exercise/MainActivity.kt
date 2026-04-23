package com.example.exercise

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.exercise.controller.WorkoutController
import com.example.exercise.repository.FirebaseRepository

class MainActivity : AppCompatActivity() {

    private lateinit var workoutController: WorkoutController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Repository and Controller
        val firebaseRepository = FirebaseRepository()
        workoutController = WorkoutController(firebaseRepository)

        // Minimal Test: Add a workout
        testBackend()
    }

    private fun testBackend() {
        Log.d("ExerciseApp", "Starting Backend Test...")

        // 1. Add a sample workout
        workoutController.addWorkout(
            date = "2023-10-27",
            exercise = "Bench Press",
            sets = 3,
            reps = 10,
            weight = 60.0
        )
        Log.d("ExerciseApp", "Workout added attempt made.")

        // 2. Fetch workouts and log them
        workoutController.fetchWorkouts { workouts ->
            Log.d("ExerciseApp", "Fetched ${workouts.size} workouts:")
            workouts.forEach { workout ->
                Log.d("ExerciseApp", " - ${workout.exercise} on ${workout.date}: ${workout.sets}x${workout.reps} @ ${workout.weight}kg (ID: ${workout.id})")
            }
        }
    }
}
