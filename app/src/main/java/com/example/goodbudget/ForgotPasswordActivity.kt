package com.example.goodbudget

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val newPass = findViewById<EditText>(R.id.newPasswordField)
        val confirmPass = findViewById<EditText>(R.id.reenterNewPasswordField)
        val resetBtn = findViewById<Button>(R.id.resetPasswordBtn)

        resetBtn.setOnClickListener {
            val pass1 = newPass.text.toString()
            val pass2 = confirmPass.text.toString()
            val isValid = validatePassword(pass1)

            if (pass1 == pass2 && isValid) {
                // Store temp password using SharedPreferences (or Singleton)
                val sharedPrefs = getSharedPreferences("ResetPrefs", MODE_PRIVATE)
                sharedPrefs.edit()
                    .putString("newPassword", pass1)
                    .apply()

                startActivity(Intent(this, OtpConfirmationActivity::class.java))
            } else {
                Toast.makeText(this, "Password doesn't match or is invalid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validatePassword(password: String): Boolean {
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        val hasDigit = password.any { it.isDigit() }
        return password.length in 8..12 && hasSpecial && hasDigit
    }
}