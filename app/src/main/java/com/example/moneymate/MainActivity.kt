package com.example.moneymate

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.moneymate.databinding.ActivitySignupBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        // Handle back button click
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        
        // Handle create account button click
        binding.btnCreateAccount.setOnClickListener {
            // TODO: Implement account creation logic
        }
        
        // Handle login text click
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}