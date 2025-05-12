package com.example.goodbudget

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class OtpConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_confirmation)

        val otpField = findViewById<EditText>(R.id.otpField)
        val verifyBtn = findViewById<Button>(R.id.verifyBtn)

        otpField.setText("12345")

        verifyBtn.setOnClickListener {
            val sharedPrefs = getSharedPreferences("ResetPrefs", MODE_PRIVATE)
            val newPassword = sharedPrefs.getString("newPassword", "") ?: ""
            val email = sharedPrefs.getString("email", "") ?: ""

            if (otpField.text.toString() == "12345" && email.isNotEmpty()) {
                val db = AppDatabase.getDatabase(applicationContext)
                val userDao = db.userDao()

                lifecycleScope.launch {
                    val user = userDao.getUserByEmail(email)
                    if (user != null) {
                        val updatedUser = user.copy(password = newPassword)
                        userDao.updateUser(updatedUser)
                        Toast.makeText(this@OtpConfirmationActivity, "Password updated", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@OtpConfirmationActivity, LoginActivity::class.java))
                    } else {
                        Toast.makeText(this@OtpConfirmationActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
