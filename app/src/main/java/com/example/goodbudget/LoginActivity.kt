package com.example.goodbudget

import android.content.Intent
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

        emailField = findViewById(R.id.emailLoginField)
        passwordField = findViewById(R.id.passwordLoginField)

        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val forgotPasswordLink = findViewById<TextView>(R.id.forgotPasswordLink)
        val registerRedirect = findViewById<TextView>(R.id.registerRedirect)

        loginBtn.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            val db = AppDatabase.getDatabase(applicationContext)
            val userDao = db.userDao()

            lifecycleScope.launch {
                val user = userDao.login(email, password)
                if (user != null) {
                    startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                } else {
                    emailField.setBackgroundColor(Color.RED)
                    passwordField.setBackgroundColor(Color.RED)
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
