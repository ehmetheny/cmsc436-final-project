package com.example.exercise.controller

import com.example.exercise.model.Exercise
import com.example.exercise.repository.FirebaseRepository
import kotlin.math.round
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryController {
    private val repository: FirebaseRepository
    private var cardioSessionStatus : Boolean = false
    private var cardioSessionDuration : Double = 0.0
    private var cardioSessionDistanceMeters : Double = 0.0
    private var cardioStartTimeMillis: Long = 0L

    constructor( rep : FirebaseRepository ) {
        repository = rep
    }

    fun getCardioSessionStatus() : Boolean {
        return cardioSessionStatus
    }

    fun startCardioSession() {
        if (cardioSessionStatus) return
        cardioSessionStatus = true
        cardioStartTimeMillis = System.currentTimeMillis()
        cardioSessionDuration = 0.0
        cardioSessionDistanceMeters = 0.0

        // start tracking time with timer/chromometer
        // start tracking distance with gps
    }

    fun addCardioDistanceMeters(deltaMeters: Double) {
        if (!cardioSessionStatus || deltaMeters <= 0.0) return
        cardioSessionDistanceMeters += deltaMeters
    }

    fun getCardioDistanceMiles(): Double {
        return round((cardioSessionDistanceMeters * METERS_TO_MILES) * 100) / 100.0
    }

    fun getCardioElapsedMillis(): Long {
        if (!cardioSessionStatus || cardioStartTimeMillis == 0L) return 0L
        return (System.currentTimeMillis() - cardioStartTimeMillis).coerceAtLeast(0L)
    }

    fun stopCardioSession() {
        if (!cardioSessionStatus) return
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = formatter.format(Date())

        cardioSessionStatus = false
        val endTimeMillis = System.currentTimeMillis()
        val elapsedMillis = (endTimeMillis - cardioStartTimeMillis).coerceAtLeast(0L)
        cardioSessionDuration = round((elapsedMillis / 60000.0) * 100) / 100.0
        cardioStartTimeMillis = 0L

        if (cardioSessionDuration <= 0.0) return

        val cardio = Exercise.Cardio(
            date = currentDate,
            duration = cardioSessionDuration,
            distance = getCardioDistanceMiles()
        )
        repository.addCardioSession(cardio)
    }

    fun addWorkout(
        date: String,
        activity: String,
        sets: Int,
        reps: Int,
        weight: Double,
        weightUnit: String,
        intensity: String
    ) {
        val workout = Exercise.Workout(
            date = date,
            activity = activity,
            sets = sets,
            reps = reps,
            weight = weight,
            weightUnit = weightUnit,
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

    companion object {
        private const val METERS_TO_MILES = 0.000621371
    }
}
