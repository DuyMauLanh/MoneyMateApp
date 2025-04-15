package com.example.moneymate

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.moneymate.database.AppDatabase
import com.example.moneymate.databinding.ActivitySignupBinding
import com.example.moneymate.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "moneyapp.db"
        ).fallbackToDestructiveMigration().build()
    }

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

            val userDao = db.userDao()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            lifecycleScope.launch {
                try {
                    val existingUser = withContext(Dispatchers.IO) {
                        userDao.getUserByEmail(email)
                    }

                    if (existingUser != null) {
                        Toast.makeText(this@SignupActivity, "Email đã được sử dụng", Toast.LENGTH_SHORT).show()
                    } else {
                        withContext(Dispatchers.IO) {
                            userDao.insert(User(name = name, email = email, password = password))
                        }
                        Toast.makeText(this@SignupActivity, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                        finish()
                    }
                } catch (e: Exception) {
                    println("Lỗi: ${e.message}")
                    Toast.makeText(this@SignupActivity, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

        }

        // Handle login text click
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
} 