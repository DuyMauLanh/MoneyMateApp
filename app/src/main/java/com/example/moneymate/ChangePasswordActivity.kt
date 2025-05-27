package com.example.moneymate

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.moneymate.dao.UserDao
import com.example.moneymate.database.AppDatabase
import com.example.moneymate.database.DatabaseProvider
import kotlinx.coroutines.launch

class ChangePasswordActivity : AppCompatActivity() {
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "moneyapp.db"
        ).build()
    }
    private lateinit var edtCurrentPassword: EditText
    private lateinit var edtNewPassword: EditText
    private lateinit var edtConfirmPassword: EditText
    private lateinit var btnChangePassword: Button

    private lateinit var userDao: UserDao
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_change_password)

        // Ánh xạ
        edtCurrentPassword = findViewById(R.id.currentPasswordInput)
        edtNewPassword = findViewById(R.id.newPasswordInput)
        edtConfirmPassword = findViewById(R.id.confirmPasswordInput)
        btnChangePassword = findViewById(R.id.changePasswordButton)

        // Lấy UserDao và ID người dùng hiện tại
        userDao = db.userDao()
        currentUserId = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1)


        btnChangePassword.setOnClickListener {
            val currentPassword = edtCurrentPassword.text.toString()
            val newPassword = edtNewPassword.text.toString()
            val confirmPassword = edtConfirmPassword.text.toString()

            if (newPassword != confirmPassword) {
                Toast.makeText(this,
                    getString(R.string.m_t_kh_u_x_c_nh_n_kh_ng_kh_p), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Chạy coroutine để truy vấn DB
            lifecycleScope.launch {
                val user = userDao.getUserById(currentUserId)
                if (user == null) {
                    Toast.makeText(this@ChangePasswordActivity,
                        getString(R.string.kh_ng_t_m_th_y_ng_i_d_ng), Toast.LENGTH_SHORT).show()
                    return@launch
                }

                if (user.password != currentPassword) {
                    Toast.makeText(this@ChangePasswordActivity,
                        getString(R.string.m_t_kh_u_hi_n_t_i_kh_ng_ng), Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Cập nhật mật khẩu mới
                user.password = newPassword
                userDao.update(user)
                Toast.makeText(this@ChangePasswordActivity,
                    getString(R.string.i_m_t_kh_u_th_nh_c_ng), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
