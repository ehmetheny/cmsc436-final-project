package com.example.exercise.repository

import com.example.exercise.model.Workout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()
    private val workoutsCollection = db.collection("workouts")

    fun addWorkout(workout: Workout) {
        // If ID is empty, Firestore will generate one
        val docRef = if (workout.id.isEmpty()) {
            workoutsCollection.document()
        } else {
            workoutsCollection.document(workout.id)
        }

        val workoutWithId = workout.copy(id = docRef.id)
        docRef.set(workoutWithId)
    }

    fun getWorkouts(callback: (List<Workout>) -> Unit) {
        workoutsCollection.get()
            .addOnSuccessListener { result ->
                val workouts = result.documents.mapNotNull { it.toObject<Workout>() }
                callback(workouts)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun deleteWorkout(id: String) {
        workoutsCollection.document(id).delete()
    }
}
