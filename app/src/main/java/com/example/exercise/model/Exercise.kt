package com.example.exercise.model

sealed class Exercise {
    abstract val date: String
    data class Workout (
        val id : String = "",
        override val date : String = "",
        val activity : String = "",
        val sets : Int = 0,
        val reps : Int = 0,
        val weight : Double = 0.0,
        val intensity : String = "",
        val type: String = "workout"
    ) : Exercise()

    data class Cardio (
        val id: String = "",
        override val date: String = "",
        val distance: Double = 0.0,
        val duration: Double = 0.0,
        val type: String = "cardio"
    ) : Exercise()
}