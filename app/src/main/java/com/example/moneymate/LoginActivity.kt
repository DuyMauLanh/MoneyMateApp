package com.example.moneymate

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moneymate.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        // Handle back button click
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        // Login button click
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (validateCredentials(email, password)) {
                // Navigate to TransactionActivity
                startActivity(Intent(this, TransactionActivity::class.java))
                finish() // Close LoginActivity
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }

        // Forgot password click
        binding.tvForgotPassword.setOnClickListener {
            // TODO: Implement forgot password functionality
            Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show()
        }

        // Sign up text click
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun validateCredentials(email: String, password: String): Boolean {
        // Check for admin credentials
        return email == "admin@gmail.com" && password == "admin"
    }
} 