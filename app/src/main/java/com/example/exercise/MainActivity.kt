package com.example.exercise

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Button
import android.widget.CalendarView
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.exercise.controller.DiaryController
import com.example.exercise.location.LocationTracker
import com.example.exercise.model.Exercise
import com.example.exercise.repository.FirebaseRepository
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class MainActivity : AppCompatActivity() {
    private lateinit var calendarView : CalendarView
    private lateinit var sessionStatus : TextView
    private lateinit var sessionTimer : TextView
    private lateinit var sessionDistance : TextView
    private lateinit var statusCircle : View
    private lateinit var addWorkoutBtn : Button
    private lateinit var trackCardioBtn : Button
    private lateinit var adView: AdView
    private lateinit var locationTracker: LocationTracker
    private var pendingCardioStartAfterPermission = false
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            val elapsedMillis = diary.getCardioElapsedMillis()
            sessionTimer.text = getString(R.string.session_timer_format, formatElapsedTime(elapsedMillis))
            sessionDistance.text = getString(R.string.session_distance_format, diary.getCardioDistanceMiles())
            if (diary.getCardioSessionStatus()) {
                timerHandler.postDelayed(this, 1000L)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Initialize Repository and Controller
        val firebaseRepository = FirebaseRepository()
        diary = DiaryController( firebaseRepository )

        // Minimal Test: Add a workout
        //testBackend()

        calendarView = findViewById<CalendarView>(R.id.calendar)
        sessionStatus = findViewById<TextView>( R.id.session_status )
        sessionTimer = findViewById<TextView>(R.id.session_timer)
        sessionDistance = findViewById<TextView>(R.id.session_distance)
        statusCircle = findViewById<View>( R.id.status_circle )
        addWorkoutBtn = findViewById<Button>( R.id.add_workout )
        trackCardioBtn = findViewById<Button>( R.id.track_cardio )
        adView = findViewById(R.id.adView)
        locationTracker = LocationTracker(this) { deltaMeters ->
            diary.addCardioDistanceMeters(deltaMeters)
        }
        MobileAds.initialize(this)
        adView.loadAd(AdRequest.Builder().build())

        addWorkoutBtn.setOnClickListener{ addWorkout() }
        trackCardioBtn.setOnClickListener{ trackCardio() }

        calendarView.setOnDateChangeListener { _, year, month, day ->
            val date = if (month + 1 < 10) {
                "$year-0${month + 1}-$day"
            } else {
                "$year-${month + 1}-$day"
            }
            openLog( date )
        }

        updateSessionStatus()
    }

    private fun testBackend() {
        Log.d("ExerciseApp", "Starting Backend Test...")

        // 1. Add a sample workout
        diary.addWorkout(
            date = "2023-10-27",
            activity = "Bench Press",
            sets = 3,
            reps = 10,
            weight = 60.0,
            weightUnit = "kg",
            intensity = "High"
        )
        Log.d("ExerciseApp", "Workout added attempt made.")

        // 2. Fetch workouts and log them
        diary.fetchWorkouts { diaryEntries ->
            Log.d("ExerciseApp", "Fetched ${diaryEntries.size} workouts:")

            diaryEntries.forEach { exercise ->
                when (exercise) {
                    is Exercise.Workout ->
                        Log.d("ExerciseApp", " - ${exercise.activity} on " +
                                "${exercise.date}: ${exercise.sets}x${exercise.reps} @ " +
                                "${exercise.weight}${exercise.weightUnit} (ID: ${exercise.id})")
                    is Exercise.Cardio ->
                        Log.d("ExerciseApp", " - ${exercise.duration} minute " +
                                "cardio session across ${exercise.distance} miles on" +
                                " ${exercise.date} (ID: ${exercise.id})")
                }
            }
        }
    }

    fun openLog(date : String) {
        val intent : Intent = Intent( this, LogActivity::class.java )
        intent.putExtra("date", date)
        startActivity( intent )
    }


    fun addWorkout() {
        val intent : Intent = Intent( this, AddActivity::class.java )
        startActivity( intent )
    }

    fun trackCardio() {
        if (diary.getCardioSessionStatus()) {
            locationTracker.stop()
            diary.stopCardioSession()
        } else {
            startCardioWithPermissionCheck()
        }
        updateSessionStatus()
    }

    private fun startCardioWithPermissionCheck() {
        if (isHasLocationPermission()) {
            diary.startCardioSession()
            locationTracker.start()
            return
        }
        pendingCardioStartAfterPermission = true
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun isHasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) return
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (pendingCardioStartAfterPermission) {
                diary.startCardioSession()
                locationTracker.start()
                updateSessionStatus()
            }
            pendingCardioStartAfterPermission = false
        } else {
            pendingCardioStartAfterPermission = false
            sessionStatus.text = "Location permission is required for cardio GPS tracking."
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationTracker.stop()
        timerHandler.removeCallbacks(timerRunnable)
    }

    fun updateSessionStatus() {
        if (diary.getCardioSessionStatus()) {
            sessionStatus.text = "Cardio session IN PROGRESS"
            statusCircle.setBackgroundColor(Color.parseColor("#1e792c"))
            timerHandler.removeCallbacks(timerRunnable)
            timerHandler.post(timerRunnable)
        } else {
            sessionStatus.text = "Cardio session NOT in progress"
            statusCircle.setBackgroundColor(Color.parseColor("#b80f0a"))
            timerHandler.removeCallbacks(timerRunnable)
            sessionTimer.text = getString(R.string.session_timer_default)
            sessionDistance.text = getString(R.string.session_distance_default)
        }
    }

    private fun formatElapsedTime(elapsedMillis: Long): String {
        val totalSeconds = elapsedMillis / 1000L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return String.format("%02d:%02d", minutes, seconds)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
        lateinit var diary : DiaryController
    }
}
