package com.example.moneymate

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.moneymate.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {
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
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }

        // Handle create account button click
        binding.btnCreateAccount.setOnClickListener {
            val name: String = binding.etName.text.toString().trim()
            val email: String = binding.etEmail.text.toString().trim()
            val password: String = binding.etPassword.text.toString().trim()

            val values: ContentValues = ContentValues()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            values.put("name", name)
            values.put("password", password)
            values.put("email", email)

            // Thêm user vào DB
            val db = openOrCreateDatabase("moneynote.db", MODE_PRIVATE, null)

            try {
                val createTableQuery = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL
                );
            """.trimIndent()

                db.execSQL(createTableQuery)
            } catch (e: Exception) {
                println("exist table")
            }

            val result = db.insert("users", null, values)

            try {
                if (result == -1L) {
                    Toast.makeText(this, "Lỗi khi tạo tài khoản", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Handle login text click
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
} 