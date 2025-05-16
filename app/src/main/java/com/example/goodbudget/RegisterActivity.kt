package com.example.goodbudget

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameField: EditText
    private lateinit var surnameField: EditText
    private lateinit var usernameField: EditText
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var confirmPasswordField: EditText

    private lateinit var radioChar: RadioButton
    private lateinit var radioSpec: RadioButton
    private lateinit var radioNum: RadioButton

    private lateinit var registerBtn: Button
    private lateinit var loginRedirect: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Bind UI elements
        nameField = findViewById(R.id.nameInput)
        surnameField = findViewById(R.id.surnameInput)
        usernameField = findViewById(R.id.usernameInput)
        emailField = findViewById(R.id.emailInput)
        passwordField = findViewById(R.id.passwordInput)
        confirmPasswordField = findViewById(R.id.confirmPasswordInput)

        radioChar = findViewById(R.id.characterRadioButton)
        radioSpec = findViewById(R.id.specialRadioButton)
        radioNum = findViewById(R.id.numericalRadioButton)

        registerBtn = findViewById(R.id.btnRegister)
        loginRedirect = findViewById(R.id.signInText)

        // Password validation indicators
        passwordField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                radioChar.isChecked = text.length in 8..12
                radioSpec.isChecked = text.any { !it.isLetterOrDigit() }
                radioNum.isChecked = text.any { it.isDigit() }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Register button logic
        registerBtn.setOnClickListener {
            val pass = passwordField.text.toString()
            val confirm = confirmPasswordField.text.toString()

            if (pass == confirm && radioChar.isChecked && radioSpec.isChecked && radioNum.isChecked) {
                val user = User(
                    name = nameField.text.toString(),
                    surname = surnameField.text.toString(),
                    username = usernameField.text.toString(),
                    email = emailField.text.toString(),
                    password = pass
                )

                val db = AppDatabase.getDatabase(applicationContext)
                val userDao = db.userDao()

                lifecycleScope.launch {
                    val existingUser = userDao.getUserByEmail(user.email)
                    if (existingUser == null) {
                        userDao.insertUser(user)
                        Toast.makeText(this@RegisterActivity, "Registration Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, DashboardActivity::class.java))
                    } else {
                        Toast.makeText(this@RegisterActivity, "Email already registered", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Password validation failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Redirect to Login
        loginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}