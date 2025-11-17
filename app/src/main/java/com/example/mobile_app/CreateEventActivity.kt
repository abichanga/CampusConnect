package com.example.mobile_app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_app.databinding.ActivityCreateEventBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEventBinding

    private val calendar = Calendar.getInstance()
    private var selectedDateTimeMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackCreateEvent.setOnClickListener {
            finish()
        }

        setupFacultySpinner()
        setupDateTimeButtons()
        setupSaveButton()
    }

    private fun setupFacultySpinner() {
        val faculties = listOf(
            "Select faculty",
            "All faculties",
            "ICS",
            "Law",
            "Business",
            "Humanities",
            "Other"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            faculties
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFaculty.adapter = adapter
    }

    private fun setupDateTimeButtons() {
        binding.btnSelectDate.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                binding.tvSelectedDate.text = sdf.format(calendar.time)

                updateDateTimeMillis()
            }, year, month, day).show()
        }

        binding.btnSelectTime.setOnClickListener {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, h, min ->
                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, min)

                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                binding.tvSelectedTime.text = sdf.format(calendar.time)

                updateDateTimeMillis()
            }, hour, minute, true).show()
        }
    }

    private fun updateDateTimeMillis() {
        selectedDateTimeMillis = calendar.timeInMillis
    }

    private fun setupSaveButton() {
        binding.btnSaveEvent.setOnClickListener {
            saveEventToFirestore()
        }
    }

    private fun saveEventToFirestore() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val venue = binding.etVenue.text.toString().trim()
        val faculty = binding.spinnerFaculty.selectedItem.toString()
        val maxAttendeesText = binding.etMaxAttendees.text.toString().trim()
        val maxAttendees = maxAttendeesText.toIntOrNull() ?: 0

        if (title.isEmpty() || description.isEmpty() || venue.isEmpty()) {
            showToast("Please fill in title, description and venue")
            return
        }

        if (faculty == "Select faculty") {
            showToast("Please select a faculty")
            return
        }

        if (selectedDateTimeMillis == 0L) {
            showToast("Please select date and time")
            return
        }

        binding.btnSaveEvent.isEnabled = false

        val currentUserId = Firebase.auth.currentUser?.uid ?: "unknown"

        val eventMap = hashMapOf(
            "title" to title,
            "description" to description,
            "venue" to venue,
            "faculty" to faculty,
            "maxAttendees" to maxAttendees,
            "dateTime" to selectedDateTimeMillis,
            "createdBy" to currentUserId
        )

        val db = Firebase.firestore

        db.collection("events")
            .add(eventMap)
            .addOnSuccessListener {
                showToast("Event created!")
                finish() // go back to admin main
            }
            .addOnFailureListener { e ->
                showToast("Failed to create event: ${e.message}")
                binding.btnSaveEvent.isEnabled = true
            }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
