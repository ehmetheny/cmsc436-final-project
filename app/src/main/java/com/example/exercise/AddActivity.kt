package com.example.exercise

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.exercise.controller.DiaryController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.edit

class AddActivity : AppCompatActivity() {
    private lateinit var activity : EditText
    private lateinit var sets : EditText
    private lateinit var reps : EditText
    private lateinit var weight : EditText
    private lateinit var weightUnitLabel : TextView
    private lateinit var usePoundsSwitch : Switch
    private lateinit var intensitySeekBar : SeekBar
    private lateinit var intensityPreview : TextView
    private lateinit var submitBtn : Button
    private lateinit var cancelBtn : Button

    companion object {
        private const val PREFS_NAME = "exercise_prefs"
        private const val PREF_DEFAULT_INTENSITY = "default_intensity"
        private const val PREF_USE_POUNDS = "use_pounds"
        private const val INTENSITY_LOW = "Low"
        private const val INTENSITY_MEDIUM = "Medium"
        private const val INTENSITY_HIGH = "High"
    }

    private val prefs by lazy {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        // just added this i got too lazy callin it everytime
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate( savedInstanceState )

        setContentView(R.layout.activity_add)

        activity = findViewById( R.id.activity_input )
        sets = findViewById( R.id.sets_input )
        reps = findViewById( R.id.reps_input )
        weight = findViewById( R.id.weight_input )
        weightUnitLabel = findViewById(R.id.weight_unit_label)
        usePoundsSwitch = findViewById(R.id.use_pounds_switch)
        intensitySeekBar = findViewById(R.id.intensity_seekbar)
        intensityPreview = findViewById(R.id.intensity_preview)
        submitBtn = findViewById( R.id.submit )
        cancelBtn = findViewById( R.id.cancel )

        loadPreferences()
        bindSeekBarListener()
        bindUnitPreferenceListener()

        submitBtn.setOnClickListener{ submit() }
        cancelBtn.setOnClickListener{ finish() }
    }

    fun submit() {
        val diary : DiaryController = MainActivity.diary
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = formatter.format(Date())
        val activity = activity.text.toString()
        val sets = sets.text.toString()
        val reps = reps.text.toString()
        val weightInput = weight.text.toString()
        val intensity = intensityFromProgress(intensitySeekBar.progress)
        var isInputValid = true

        if (activity == "" || sets == "" || reps == "" || weightInput == "") {
            isInputValid = false
        }

        if (isInputValid) {
            val setsValue = sets.toIntOrNull()
            val repsValue = reps.toIntOrNull()
            val weightValue = weightInput.toDoubleOrNull()

            if (setsValue == null || repsValue == null || weightValue == null) {
                Toast.makeText(this, "Please enter valid numeric values.", Toast.LENGTH_LONG).show()
                return
            }

            val shouldUsePounds = usePoundsSwitch.isChecked
            val weightUnit = if (shouldUsePounds) "lb" else "kg"

            diary.addWorkout(
                date = currentDate,
                activity = activity,
                sets = setsValue,
                reps = repsValue,
                weight = weightValue,
                weightUnit = weightUnit,
                intensity = intensity
            )

            prefs.edit {
                putString(PREF_DEFAULT_INTENSITY, intensity)
                putBoolean(PREF_USE_POUNDS, shouldUsePounds)
            }

            finish()
        } else {
            Toast.makeText(this, "Please complete all fields.", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadPreferences() {
        val savedIntensity = prefs.getString(PREF_DEFAULT_INTENSITY, INTENSITY_MEDIUM) ?: INTENSITY_MEDIUM
        val isUsePounds = prefs.getBoolean(PREF_USE_POUNDS, true)

        usePoundsSwitch.isChecked = isUsePounds
        weightUnitLabel.text = if (isUsePounds) getString(R.string.weight_unit_lb) else getString(R.string.weight_unit_kg)

        intensitySeekBar.progress = progressFromIntensity(savedIntensity)
        updateIntensityPreview(savedIntensity)
    }

    private fun bindUnitPreferenceListener() {
        usePoundsSwitch.setOnCheckedChangeListener { _, isChecked ->
            weightUnitLabel.text = if (isChecked) getString(R.string.weight_unit_lb) else getString(R.string.weight_unit_kg)
            prefs.edit { putBoolean(PREF_USE_POUNDS, isChecked) }
        }
    }

    private fun bindSeekBarListener() {
        intensitySeekBar.max = 2
        intensitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val intensity = intensityFromProgress(progress)
                updateIntensityPreview(intensity)
                prefs.edit { putString(PREF_DEFAULT_INTENSITY, intensity) }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }

    private fun updateIntensityPreview(intensity: String) {
        intensityPreview.text = getString(R.string.intensity_selected, intensity)
    }

    private fun intensityFromProgress(progress: Int): String {
        return when (progress) {
            0 -> INTENSITY_LOW
            2 -> INTENSITY_HIGH
            else -> INTENSITY_MEDIUM
        }
    }

    // reverse of intensityFromProgress
    // just mapping it from intensity -> num
    private fun progressFromIntensity(intensity: String): Int {
        return when (intensity) {
            INTENSITY_LOW -> 0
            INTENSITY_HIGH -> 2
            else -> 1
        }
    }
}