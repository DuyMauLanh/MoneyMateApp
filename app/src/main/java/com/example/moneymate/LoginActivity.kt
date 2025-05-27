package com.example.moneymate

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.moneymate.database.AppDatabase
import com.example.moneymate.databinding.ActivityLoginBinding
import com.example.moneymate.service.JavaMailAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "moneyapp.db"
        ).build()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }
    private fun handleForgotPassword(email: String) {
        val userDao = db.userDao()

        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) {
                userDao.getUserByEmail(email)
            }

            if (user == null) {
                Toast.makeText(this@LoginActivity,
                    getString(R.string.email_kh_ng_t_n_t_i), Toast.LENGTH_SHORT).show()
            } else {
                val newPassword = generateRandomPassword()

                withContext(Dispatchers.IO) {
                    user.password = newPassword
                    userDao.update(user)
                }

                sendEmailAsync(email, newPassword) { success ->
                    runOnUiThread {
                        if (success) {
                            Toast.makeText(this@LoginActivity,
                                getString(R.string.m_t_kh_u_m_i_c_g_i_n_email), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@LoginActivity,
                                getString(R.string.kh_ng_th_g_i_email_vui_l_ng_th_l_i_sau), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun sendEmailAsync(email: String, newPassword: String, callback: (Boolean) -> Unit) {
        JavaMailAPI(
            context = this,
            email = email,
            subject = getString(R.string.kh_i_ph_c_m_t_kh_u),
            message = getString(R.string.m_t_kh_u_m_i_c_a_b_n_l, newPassword)
        ) { success ->
            callback(success)
        }.execute()
    }
    fun generateRandomPassword(length: Int = 10): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length).map { chars.random() }.joinToString("")
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
                Toast.makeText(this,
                    getString(R.string.vui_l_ng_i_n_y_th_ng_tin), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userDao = db.userDao()

            lifecycleScope.launch {
                val user = withContext(Dispatchers.IO) {
                    userDao.getUserByEmail(email)
                }

                if (user != null && user.password == password) {
                    Toast.makeText(this@LoginActivity,
                        getString(R.string.ng_nh_p_th_nh_c_ng), Toast.LENGTH_SHORT).show()
                    val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    sharedPref.edit().putInt("user_id", user.id).apply()

                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity,
                        getString(R.string.email_ho_c_m_t_kh_u_kh_ng_ng), Toast.LENGTH_SHORT).show()
                }
            }
        }


        binding.tvForgotPassword.setOnClickListener {
            // TODO: Implement forgot password functionality
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.qu_n_m_t_kh_u))

            val input = EditText(this)
            input.hint = getString(R.string.nh_p_email_ng_k)
            input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            builder.setView(input)

            builder.setPositiveButton(getString(R.string.x_c_nh_n)) { _, _ ->
                val email = input.text.toString().trim()
                handleForgotPassword(email)
            }

            builder.setNegativeButton(getString(R.string.h_y), null)
            builder.show()
            Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show()
        }


        // Handle sign up text click
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }
} 