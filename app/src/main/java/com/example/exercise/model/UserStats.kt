package com.example.exercise.model

data class UserStats(
    val userId: String,
    val currentStreak: Long,
    val longestStreak: Long,
    val lastActivityDate: String,
    val displayName: String = ""
)
