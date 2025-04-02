package com.example.moneymate

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.moneymate.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        // Handle sign up button click
        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Handle login text click
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
} 