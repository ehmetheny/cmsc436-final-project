package com.example.exercise

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.exercise.MainActivity.Companion.diary
import com.example.exercise.model.User
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LeaderboardActivity : AppCompatActivity() {

    private val isoDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val myUserId = User.getOrCreate(this).id
        val listView = findViewById<ListView>(R.id.leaderboard_list)
        val emptyMessage = findViewById<TextView>(R.id.emptyMessage)

        findViewById<Button>(R.id.back).setOnClickListener { finish() }

        // leaderboard stuff: ranking logic, name, streak, longest streak, last activity date
        diary.fetchLeaderboard { rows ->
            val lines = rows.mapIndexed { index, row ->
                val rank = index + 1
                val name = row.displayName.trim()
                val label = when {
                    row.userId == myUserId ->
                        name.ifEmpty { getString(R.string.leaderboard_you) } // for some weird reason if the name is empty so we use the string "You"
                    name.isNotEmpty() -> name
                    else -> shortPlayerLabel(row.userId) // this is to shorten the user id to 8 characters and add an ellipsis if it's longer (shouldnt happen)
                }
                getString(
                    R.string.leaderboard_row,
                    rank,
                    label,
                    row.currentStreak,
                    row.longestStreak,
                    formatLastActivityDate(row.lastActivityDate)
                )
            }
            runOnUiThread {
                val adapter = ArrayAdapter(
                    this,
                    R.layout.item_leaderboard_row,
                    android.R.id.text1,
                    lines
                )
                listView.adapter = adapter
                listView.emptyView = emptyMessage
            }
        }
    }

    // turns date from yyyy-mm-dd to a readable date -> (its gonna turn into this format: May 4, 2026)
    private fun formatLastActivityDate(iso: String): String {
        val trimmed = iso.trim()
        if (trimmed.isEmpty()) {
            return getString(R.string.leaderboard_last_activity_unknown)
        }
        // try to parse the date, if it fails, return the trimmed date
        return try {
            val parsed: Date = isoDate.parse(trimmed) ?: return trimmed
            DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(parsed)
        } catch (_: ParseException) {
            trimmed
        }
    }

    private fun shortPlayerLabel(userId: String): String {
        if (userId.length <= 8) return userId
        return userId.take(8) + "…"
    }
}
