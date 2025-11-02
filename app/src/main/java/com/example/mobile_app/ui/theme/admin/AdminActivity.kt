package com.example.mobile_app.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_app.databinding.ActivityAdminBinding

class AdminActivity : ComponentActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var adapter: EventAdapter

    // Get the ViewModel we wrote in step 2
    private val viewModel: EventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate view binding
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        adapter = EventAdapter(emptyList())
        binding.eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.eventsRecyclerView.adapter = adapter

        // Observe the list of events from the ViewModel
        viewModel.events.observe(this) { eventList ->
            adapter.submitList(eventList)
        }

        // Handle Add Event button
        binding.btnAddEvent.setOnClickListener {
            val title = binding.inputTitle.text.toString().trim()
            val desc = binding.inputDescription.text.toString().trim()
            val faculty = binding.inputFaculty.text.toString().trim()
            val location = binding.inputLocation.text.toString().trim()
            val date = binding.inputDate.text.toString().trim()
            val time = binding.inputTime.text.toString().trim()

            if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Title, date, and time are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addEvent(
                title = title,
                description = desc,
                faculty = faculty,
                location = location,
                date = date,
                time = time
            )

            Toast.makeText(this, "Event added", Toast.LENGTH_SHORT).show()

            // Optional: clear the fields after saving
            binding.inputTitle.text?.clear()
            binding.inputDescription.text?.clear()
            binding.inputFaculty.text?.clear()
            binding.inputLocation.text?.clear()
            binding.inputDate.text?.clear()
            binding.inputTime.text?.clear()
        }
    }
}
