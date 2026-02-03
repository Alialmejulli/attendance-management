package com.example.aws


    data class User(
        val id: String,
        val first_name: String,
        val last_name: String,
        val email: String,
        val role: String,
        val gpa: Double?,
        val password: String,
        val major: String,
        val year: Int?,
    )

