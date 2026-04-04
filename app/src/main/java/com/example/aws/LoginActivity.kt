package com.example.aws

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import androidx.core.content.edit

class LoginActivity : BaseActivity() {

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

            btnLogin.isEnabled = false

            RetrofitClient.instance.getUsers().enqueue(object : retrofit2.Callback<UsersResponse> {

                override fun onResponse(
                    call: retrofit2.Call<UsersResponse>,
                    response: retrofit2.Response<UsersResponse>
                ) {
                    btnLogin.isEnabled = true

                    if (!response.isSuccessful) {
                        Toast.makeText(this@LoginActivity, "Server error", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val users = response.body()?.items ?: emptyList()
                    val user = users.find { it.email == email && it.password == password }

                    if (user == null) {
                        Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                        return
                    }

                    Toast.makeText(this@LoginActivity, "Welcome ${user.first_name}", Toast.LENGTH_SHORT).show()

                    when (user.role.lowercase()) {

                        "student" -> {
                            val intent = Intent(this@LoginActivity, StudentHomeActivity::class.java)

                            // Save student ID for later use
                            val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
                            prefs.edit {
                                putString("student_id", user.id)
                            }


                            // Send all student info
                            intent.putExtra("student_id", user.id)
                            intent.putExtra("first_name", user.first_name)
                            intent.putExtra("last_name", user.last_name)
                            intent.putExtra("gpa", user.gpa.toString())
                            intent.putExtra("major", user.major)


                            startActivity(intent)
                        }

                        "instructor" -> {
                            val intent = Intent(this@LoginActivity, InstructorHomeActivity::class.java)

                            intent.putExtra("instructor_id", user.id)
                            intent.putExtra("first_name", user.first_name)
                            intent.putExtra("last_name", user.last_name)
                            intent.putExtra("department", user.department)

                            startActivity(intent)
                        }

                    }

                    finish()
                }

                override fun onFailure(call: retrofit2.Call<UsersResponse>, t: Throwable) {
                    btnLogin.isEnabled = true
                    Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LOGIN", "Error", t)
                }
            })
        }
    }
}
