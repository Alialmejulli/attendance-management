package com.example.aws

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object UserRepository {

    fun loadUsers(context: Context): List<User> {
        val json = context.assets.open("users.json")
            .bufferedReader()
            .use { it.readText() }

        val type = object : TypeToken<List<User>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun login(email: String, password: String, context: Context): User? {
        val users = loadUsers(context)

        return users.firstOrNull {
            it.email.trim().equals(email.trim(), ignoreCase = true) &&
                    it.password.trim() == password.trim()
        }
    }
}
