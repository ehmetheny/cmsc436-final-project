package com.example.exercise.controller

import com.example.exercise.model.Workout
import com.example.exercise.repository.FirebaseRepository

class WorkoutController(private val repository: FirebaseRepository) {

    fun addWorkout(date: String, exercise: String, sets: Int, reps: Int, weight: Double) {
        val workout = Workout(
            date = date,
            exercise = exercise,
            sets = sets,
            reps = reps,
            weight = weight
        )
        repository.addWorkout(workout)
    }

    fun fetchWorkouts(callback: (List<Workout>) -> Unit) {
        repository.getWorkouts(callback)
    }

    fun deleteWorkout(id: String) {
        repository.deleteWorkout(id)
    }
}
