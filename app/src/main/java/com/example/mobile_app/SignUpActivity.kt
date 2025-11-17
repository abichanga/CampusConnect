package com.example.mobile_app

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import Models.User
import com.example.mobile_app.databinding.ActivitySignUpBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFacultySpinner()
        setupClickListeners()
    }

    private fun setupFacultySpinner() {
        val faculties = listOf(
            "Select faculty",
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

    private fun setupClickListeners() {
        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            signUpUser()
        }
    }

    private fun signUpUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val faculty = binding.spinnerFaculty.selectedItem.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Please fill in all fields")
            return
        }

        if (password != confirmPassword) {
            showToast("Passwords do not match")
            return
        }

        if (faculty == "Select faculty") {
            showToast("Please select your faculty")
            return
        }

        binding.btnSignUp.isEnabled = false

        val auth = Firebase.auth
        val db = Firebase.firestore

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: return@addOnSuccessListener

                // Everyone created in the app is a STUDENT by default
                val user = User(
                    uid = uid,
                    name = name,
                    email = email,
                    faculty = faculty,
                    role = "student"
                )

                db.collection("users")
                    .document(uid)
                    .set(user)
                    .addOnSuccessListener {
                        showToast("Account created! Please log in.")
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        showToast("Failed to save user profile: ${e.message}")
                        binding.btnSignUp.isEnabled = true
                    }
            }
            .addOnFailureListener { e ->
                showToast("Sign up failed: ${e.message}")
                binding.btnSignUp.isEnabled = true
            }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
