package com.example.exercise.controller

import com.example.exercise.model.Exercise
import com.example.exercise.repository.FirebaseRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryController {
    private val repository: FirebaseRepository
    private var cardioSessionStatus : Boolean = false
    private var cardioSessionDuration : Double = 0.0
    private var cardioSessionDistance : Double = 0.0

    constructor( rep : FirebaseRepository ) {
        repository = rep
    }

    fun getCardioSessionStatus() : Boolean {
        return cardioSessionStatus
    }

    fun startCardioSession() {
        cardioSessionStatus = true

        // start tracking time with timer/chromometer
        // start tracking distance with gps
    }

    fun stopCardioSession() {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = formatter.format(Date())

        cardioSessionStatus = false

        val cardio = Exercise.Cardio(
            date = currentDate,
            duration = cardioSessionDuration,
            distance = cardioSessionDistance
        )
        repository.addCardioSession(cardio)
    }

    fun addWorkout(date: String, activity: String, sets: Int, reps: Int, weight: Double, intensity: String) {
        val workout = Exercise.Workout(
            date = date,
            activity = activity,
            sets = sets,
            reps = reps,
            weight = weight,
            intensity = intensity
        )
        repository.addWorkout(workout)
    }

    fun fetchWorkouts(callback: (List<Exercise>) -> Unit) {
        repository.getWorkouts(callback)
    }

    fun deleteWorkout(id: String) {
        repository.deleteWorkout(id)
    }
}
