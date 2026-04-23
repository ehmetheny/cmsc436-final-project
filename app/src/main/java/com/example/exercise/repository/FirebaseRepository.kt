package com.example.exercise.repository

import com.example.exercise.model.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val workoutsCollection = db.collection("workouts")

    fun addCardioSession(cardio: Exercise.Cardio) {
        // If ID is empty, Firestore will generate one
        val docRef = if (cardio.id.isEmpty()) {
            workoutsCollection.document()
        } else {
            workoutsCollection.document(cardio.id)
        }

        val cardioWithId = cardio.copy(id = docRef.id)
        docRef.set(cardioWithId)
    }

    fun addWorkout(workout: Exercise.Workout) {
        // If ID is empty, Firestore will generate one
        val docRef = if (workout.id.isEmpty()) {
            workoutsCollection.document()
        } else {
            workoutsCollection.document(workout.id)
        }

        val workoutWithId = workout.copy(id = docRef.id)
        docRef.set(workoutWithId)
    }

    fun getWorkouts(callback: (List<Exercise>) -> Unit) {
        workoutsCollection.get()
            .addOnSuccessListener { result ->
                val exercises = result.documents.mapNotNull { exercise ->
                    when (exercise.getString("type")) {
                        "workout" -> Exercise.Workout(
                            id = exercise.id,
                            date = exercise.getString("date") ?: "",
                            activity = exercise.getString("activity") ?: "",
                            sets = exercise.getLong("sets")?.toInt() ?: 0,
                            reps = exercise.getLong("reps")?.toInt() ?: 0,
                            weight = exercise.getDouble("weight") ?: 0.0,
                            intensity = exercise.getString("intensity") ?: "",
                            type = "workout"
                        )
                        "cardio" -> Exercise.Cardio(
                            id = exercise.id,
                            date = exercise.getString("date") ?: "",
                            distance = exercise.getDouble("distance") ?: 0.0,
                            duration = exercise.getDouble("duration") ?: 0.0,
                            type = "cardio"
                        )
                        else -> null
                    }
                }
                callback(exercises)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun deleteWorkout(id: String) {
        workoutsCollection.document(id).delete()
    }
}
