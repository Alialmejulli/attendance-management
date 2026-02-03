package com.example.aws

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val emailField = findViewById<EditText>(R.id.id_input)
        val passwordField = findViewById<EditText>(R.id.password_input)
        val btnLogin = findViewById<Button>(R.id.login_button)

        btnLogin.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty()) {
                emailField.error = "Enter your email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordField.error = "Enter your password"
                return@setOnClickListener
            }

            Log.d("DEBUG", "Before login check")
            val user = UserRepository.login(email, password, this)
            Log.d("DEBUG", "After login check")

            if (user == null) {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Welcome ${user.first_name}", Toast.LENGTH_SHORT).show()

            when (user.role.lowercase()) {
                "student" -> startActivity(Intent(this, StudentHomeActivity::class.java))
                "instructor" -> startActivity(Intent(this, InstructorHomeActivity::class.java))
                else -> {
                    Toast.makeText(this, "Unknown role: ${user.role}", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            finish()
        }
    }
}
