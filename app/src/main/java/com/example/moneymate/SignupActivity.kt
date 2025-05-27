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
import com.google.android.material.checkbox.MaterialCheckBox
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
            val cbTerms: MaterialCheckBox = findViewById(R.id.cbTerms)
            var isValid = true
            val userDao = db.userDao()
            binding.tilEmail.error = null
            binding.tilPassword.error = null

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                binding.tilEmail.error = "Email không được để trống"
                isValid = false
            } else if (!email.endsWith("@gmail.com", ignoreCase = true)) {
                binding.tilEmail.error = "Email phải kết thúc bằng @gmail.com"
                isValid = false
            }

            if (password.isEmpty()) {
                binding.tilPassword.error = "Mật khẩu không được để trống"
                isValid = false
            } else if (password.length < 8) {
                binding.tilPassword.error = "Mật khẩu phải có ít nhất 8 ký tự"
                isValid = false
            }
            if (!isValid) return@setOnClickListener
            if (!cbTerms.isChecked) {
                Toast.makeText(this, "Bạn cần đồng ý với chính sách bảo mật để tiếp tục.", Toast.LENGTH_SHORT).show()
            } else {
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
        }

        // Handle login text click
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
} 