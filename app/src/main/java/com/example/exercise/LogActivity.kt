package com.example.exercise

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.exercise.MainActivity.Companion.diary
import com.example.exercise.model.Exercise

class LogActivity : AppCompatActivity() {
    private var date : String? = ""
    private lateinit var title : TextView
    private lateinit var listView : ListView
    private lateinit var backBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate( savedInstanceState )

        setContentView(R.layout.activity_log)

        date = intent.getStringExtra("date")

        title = findViewById<TextView>( R.id.title )
        listView = findViewById<ListView>(R.id.workouts)
        backBtn = findViewById<Button>( R.id.back )

        title.text = date

        // list diary entries
        diary.fetchWorkouts { exercises ->
            val items = mutableListOf<String>()
            val diaryEntries = mutableListOf<Exercise>()

            exercises.filter { it.date == date }.forEach { exercise ->
                diaryEntries.add(exercise)
                when (exercise) {
                    is Exercise.Workout -> items.add("${exercise.activity} - ${exercise.sets}x${exercise.reps} @ ${exercise.weight}kg")
                    is Exercise.Cardio -> items.add("Cardio - ${exercise.duration} mins, ${exercise.distance} miles")
                }
            }

            runOnUiThread {
                listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)

                // make diary entries deletable
                listView.setOnItemClickListener { _, _, position, _ ->
                    val id = when (val selected = diaryEntries[position]) {
                        is Exercise.Workout -> selected.id
                        is Exercise.Cardio -> selected.id
                    }
                    diary.deleteWorkout(id)
                    finish()
                }
            }
        }

        backBtn.setOnClickListener{ finish() }

        Toast.makeText(this, "Click on an entry to delete it.", Toast.LENGTH_LONG).show()
    }
}