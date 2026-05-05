package com.example.exercise.model

import android.content.Context
import java.util.UUID
import com.example.exercise.AddActivity
import androidx.core.content.edit

class User(val id: String) {

    companion object {
        // btw just reusing the prefs name from the same as the one in the AddActivity class "exercise_prefs"
        const val PREFS_NAME = AddActivity.PREFS_NAME
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val MAX_DISPLAY_NAME_LENGTH = 24

        fun getOrCreate(context: Context): User {
            val prefs = context.getSharedPreferences(AddActivity.PREFS_NAME, Context.MODE_PRIVATE)
            val existing = prefs.getString(KEY_USER_ID, null)
            if (existing != null) return User(existing)
            val id = UUID.randomUUID().toString()
            prefs.edit { putString(KEY_USER_ID, id) }
            return User(id)
        }

        fun getDisplayName(context: Context): String =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_DISPLAY_NAME, "").orEmpty()

        fun setDisplayName(context: Context, raw: String) {
            val trimmed = raw.trim().take(MAX_DISPLAY_NAME_LENGTH)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
                putString(KEY_DISPLAY_NAME, trimmed)
            }
        }
    }
}
