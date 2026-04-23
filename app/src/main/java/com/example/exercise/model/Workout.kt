package com.example.exercise.model

data class Workout(
    val id: String = "",
    val date: String = "",
    val exercise: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val weight: Double = 0.0
)
