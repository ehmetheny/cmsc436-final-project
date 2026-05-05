package com.example.exercise

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.exercise.model.User
import com.example.exercise.repository.FirebaseRepository

class DisplayNameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = User.getOrCreate(this).id
        if (User.getDisplayName(this).isNotBlank()) {
            goMain()
            return
        }

        setContentView(R.layout.activity_display_name)

        val input = findViewById<EditText>(R.id.display_name_input)
        findViewById<Button>(R.id.display_name_continue).setOnClickListener {
            val raw = input.text?.toString()?.trim().orEmpty()
            if (raw.isEmpty()) {
                Toast.makeText(this, getString(R.string.leaderboard_name_missing), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            User.setDisplayName(this, raw)
            FirebaseRepository(userId).saveLeaderboardDisplayName(User.getDisplayName(this))
            goMain()
        }

        input.requestFocus()
    }

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
