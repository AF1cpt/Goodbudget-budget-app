package com.example.goodbudget.ui
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.goodbudget.R
import com.example.goodbudget.databinding.ActivityRegisterBinding
import com.example.moneybuddy.LoginActivity

class RegisterActivity : AppCompatActivity() {

    // Declare views
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var signInText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        //firstNameInput = findViewById(R.id.formLayout).findViewById(R.id.firstNameInput)
        //lastNameInput = findViewById(R.id.formLayout).findViewById(R.id.lastNameInput)
        //usernameInput = findViewById(R.id.formLayout).findViewById(R.id.usernameInput)
        //emailInput = findViewById(R.id.formLayout).findViewById(R.id.emailInput)
        //passwordInput = findViewById(R.id.formLayout).findViewById(R.id.passwordInput)
        //confirmPasswordInput = findViewById(R.id.formLayout).findViewById(R.id.confirmPasswordInput)
        //registerButton = findViewById(R.id.btnRegister)
        //signInText = findViewById(R.id.signInText)

        // Setup register button click
        registerButton.setOnClickListener {
            handleRegistration()
        }

        // Make "Sign in" clickable
        setSignInLink()
    }

    private fun handleRegistration() {
        val firstName = firstNameInput.text.toString()
        val lastName = lastNameInput.text.toString()
        val username = usernameInput.text.toString()
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        // Simple validation (you can expand this)
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty()
            || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
        ) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Save user data or register with backend/local DB

        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

        // Redirect to login screen
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setSignInLink() {
        val fullText = "Already have an account? Sign in"
        val spannable = SpannableString(fullText)

        val signInStart = fullText.indexOf("Sign in")
        val signInEnd = signInStart + "Sign in".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        spannable.setSpan(clickableSpan, signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        signInText.text = spannable
        signInText.movementMethod = LinkMovementMethod.getInstance()
    }
}

