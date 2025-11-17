package com.example.mobile_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_app.databinding.ActivityAdminMainBinding

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: later wire these to CreateEventActivity and AdminEventsActivity
        binding.btnCreateEvent.setOnClickListener {
            // startActivity(Intent(this, CreateEventActivity::class.java))
        }

        binding.btnViewEvents.setOnClickListener {
            // startActivity(Intent(this, AdminEventsActivity::class.java))
        }
    }
}
