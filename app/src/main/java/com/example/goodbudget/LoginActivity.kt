package com.example.goodbudget

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailField = findViewById(R.id.emailInput)
        passwordField = findViewById(R.id.passwordInput)

        val loginButton = findViewById<Button>(R.id.btnLogin)
        val forgotPasswordLink = findViewById<TextView>(R.id.forgotPassword)
        val registerRedirect = findViewById<TextView>(R.id.registerRedirect)

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = AppDatabase.getDatabase(applicationContext)
            val userDao = db.userDao()

            lifecycleScope.launch {
                val user = userDao.login(email, password)
                if (user != null) {

                    val sharedPref = getSharedPreferences("USER_PREF", MODE_PRIVATE)
                    sharedPref.edit().putString("email", user.email).apply()


                    startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                    finish()
                } else {
                    emailField.setBackgroundColor(Color.parseColor("#FFCDD2"))
                    passwordField.setBackgroundColor(Color.parseColor("#FFCDD2"))
                    Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }

        forgotPasswordLink.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        registerRedirect.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}