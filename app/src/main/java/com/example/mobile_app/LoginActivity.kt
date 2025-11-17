package com.example.mobile_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import Models.User
import com.example.mobile_app.databinding.ActivityLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.toObject


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvSignUpLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        binding.btnLogin.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email = binding.etEmailLogin.text.toString().trim()
        val password = binding.etPasswordLogin.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please enter email and password")
            return
        }

        binding.btnLogin.isEnabled = false

        val auth = Firebase.auth
        val db = Firebase.firestore

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: return@addOnSuccessListener

                db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val user = snapshot.toObject<User>()

                        if (user == null) {
                            showToast("User profile not found")
                            binding.btnLogin.isEnabled = true
                            return@addOnSuccessListener
                        }

                        // Missing role in older docs? Treat as student by default
                        val role = user.role.ifEmpty { "student" }

                        if (role == "admin") {
                            // ADMIN: go to admin main
                            val intent = Intent(this, AdminMainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // STUDENT: go to your existing student main (MainActivity)
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                    .addOnFailureListener { e ->
                        showToast("Failed to load profile: ${e.message}")
                        binding.btnLogin.isEnabled = true
                    }
            }
            .addOnFailureListener { e ->
                showToast("Login failed: ${e.message}")
                binding.btnLogin.isEnabled = true
            }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
