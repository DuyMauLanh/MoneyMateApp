package com.example.moneymate

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.moneymate.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Handle back button click
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }

        // Handle login button click
        binding.btnLogin.setOnClickListener {
            val email: String = binding.etEmail.text.toString().trim()
            val password: String = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check credentials in database
            val db = openOrCreateDatabase("moneynote.db", MODE_PRIVATE, null)
            val cursor = db.rawQuery(
                "SELECT * FROM users WHERE email = ? AND password = ?",
                arrayOf(email, password)
            )

            if (cursor.moveToFirst()) {
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show()
            }

            cursor.close()
            db.close()
        }

        binding.tvForgotPassword.setOnClickListener {
            // TODO: Implement forgot password functionality
            Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show()
        }

        // Handle sign up text click
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }
} 