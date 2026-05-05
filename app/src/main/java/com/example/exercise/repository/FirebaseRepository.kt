package com.example.exercise.repository

import com.example.exercise.model.Exercise
import com.example.exercise.model.UserStats
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Locale

class FirebaseRepository(private val userId: String) {
    private val db = FirebaseFirestore.getInstance()
    private val workoutsCollection = db.collection("workouts")
    private val userStatsCollection = db.collection("userStats")

    fun addCardioSession(cardio: Exercise.Cardio) {
        val docRef = if (cardio.id.isEmpty()) {
            workoutsCollection.document()
        } else {
            workoutsCollection.document(cardio.id)
        }

        val cardioWithId = cardio.copy(id = docRef.id, userId = userId)
        docRef.set(cardioWithId)
            .addOnSuccessListener {
                applyStreakForActivityDate(cardioWithId.date)
            }
    }

    fun addWorkout(workout: Exercise.Workout) {
        val docRef = if (workout.id.isEmpty()) {
            workoutsCollection.document()
        } else {
            workoutsCollection.document(workout.id)
        }

        val workoutWithId = workout.copy(id = docRef.id, userId = userId)
        docRef.set(workoutWithId)
            .addOnSuccessListener {
                applyStreakForActivityDate(workoutWithId.date)
            }
    }


    private fun applyStreakForActivityDate(activityDate: String) {
        val statsRef = userStatsCollection.document(userId)
        statsRef.get()
            .addOnSuccessListener { snapshot ->
                val last = snapshot.getString("lastActivityDate") ?: ""
                val current = snapshot.getLong("currentStreak")?.toInt() ?: 0
                val longest = snapshot.getLong("longestStreak")?.toInt() ?: 0

                // basically says if the last activity date is not empty and the activity date is before the last activity date, 
                // then don't apply the streak
                if (last.isNotEmpty() && activityDate < last) {
                    return@addOnSuccessListener
                }

                // same day is gonna be no change in streak
                // first day ever is gonna be 1
                // next day exactly + 1 day
                // if the gap is > 1, then we skipped at least one day, streak reset to 1
                // if the gap is 0 (or negative), then it's a weird gap, just keep the current streak (shouldnt happen tho)
                val newStreak = when {
                    activityDate == last -> current
                    last.isEmpty() -> 1
                    else -> {
                        when (val gap = daysBetweenIso(last, activityDate)) {
                            1L -> current + 1
                            else -> if (gap > 1L) 1 else current
                        }
                    }
                }

                val newLongest = maxOf(longest, newStreak)
                val data = mapOf(
                    "lastActivityDate" to activityDate,
                    "currentStreak" to newStreak.toLong(),
                    "longestStreak" to newLongest.toLong()
                )
                statsRef.set(data, SetOptions.merge())
            }
    }

    fun saveLeaderboardDisplayName(name: String) {
        val data = mapOf<String, Any>("displayName" to name.trim())
        userStatsCollection.document(userId).set(data, SetOptions.merge())
    }

    fun getWorkouts(callback: (List<Exercise>) -> Unit) {
        workoutsCollection
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val exercises = result.documents.mapNotNull { exercise ->
                    when (exercise.getString("type")) {
                        "workout" -> Exercise.Workout(
                            id = exercise.id,
                            userId = exercise.getString("userId") ?: "",
                            date = exercise.getString("date") ?: "",
                            activity = exercise.getString("activity") ?: "",
                            sets = exercise.getLong("sets")?.toInt() ?: 0,
                            reps = exercise.getLong("reps")?.toInt() ?: 0,
                            weight = exercise.getDouble("weight") ?: 0.0,
                            weightUnit = exercise.getString("weightUnit") ?: "lb",
                            intensity = exercise.getString("intensity") ?: "",
                            type = "workout"
                        )
                        "cardio" -> Exercise.Cardio(
                            id = exercise.id,
                            userId = exercise.getString("userId") ?: "",
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

    fun fetchLeaderboard(
        limit: Long = 20, // idk 20 just seems reasonable and a good number :)
        callback: (List<UserStats>) -> Unit
    ) {
        userStatsCollection
            .orderBy("currentStreak", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener { snapshot ->
                val rows = snapshot.documents.map { doc ->
                    UserStats(
                        userId = doc.id,
                        currentStreak = doc.getLong("currentStreak") ?: 0L,
                        longestStreak = doc.getLong("longestStreak") ?: 0L,
                        lastActivityDate = doc.getString("lastActivityDate") ?: "",
                        displayName = doc.getString("displayName") ?: ""
                    )
                }
                callback(rows)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun deleteWorkout(id: String) {
        workoutsCollection.document(id).delete()
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
        }

        // looks a bit weird but this is the difference between two iso dates, measured in days
        private fun daysBetweenIso(earlier: String, later: String): Long {
            val a = dateFormat.parse(earlier)?.time ?: return 0L
            val b = dateFormat.parse(later)?.time ?: return 0L
            return (b - a) / (24L * 60L * 60L * 1000L) // this converts miliseconds to days (so im divinding by the number of milliseconds in a day)
        }
    }
}
