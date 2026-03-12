package com.example.aws

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.edit
import androidx.core.graphics.toColorInt


class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_page)

        // -----------------------------
        // BACK BUTTON
        // -----------------------------
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // -----------------------------
        // LOGOUT
        // -----------------------------
        findViewById<LinearLayout>(R.id.logout).setOnClickListener {

            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("yes") { _, _ ->

                    val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
                    prefs.edit().clear().apply()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("cancel", null)
                .create()

            dialog.show()
        }

        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)

        // -----------------------------
        // NOTIFICATIONS SWITCH
        // -----------------------------
        val notificationsSwitch = findViewById<SwitchCompat>(R.id.notifications_switch)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)

        notificationsSwitch.isChecked = notificationsEnabled

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit {
                putBoolean("notifications_enabled", isChecked)
            }
        }

        // -----------------------------
        // LANGUAGE BUTTONS
        // -----------------------------
        val btnEnglish = findViewById<Button>(R.id.btn_english)
        val btnArabic = findViewById<Button>(R.id.btn_arabic)

        val savedLang = prefs.getString("app_language", "en")

        // Restore selected button UI
        if (savedLang == "ar") {
            btnArabic.setBackgroundResource(R.drawable.language_selected)
            btnArabic.setTextColor(Color.WHITE)

            btnEnglish.setBackgroundResource(R.drawable.language_unselected)
            btnEnglish.setTextColor("#2E4F3D".toColorInt())
        } else {
            btnEnglish.setBackgroundResource(R.drawable.language_selected)
            btnEnglish.setTextColor(Color.WHITE)

            btnArabic.setBackgroundResource(R.drawable.language_unselected)
            btnArabic.setTextColor("#2E4F3D".toColorInt())
        }

        // English button click
        btnEnglish.setOnClickListener {
            prefs.edit { putString("app_language", "en") }
            setLocale("en")
            setResult(RESULT_OK)
            finish()
        }

        // Arabic button click
        btnArabic.setOnClickListener {
            prefs.edit { putString("app_language", "ar") }
            setLocale("ar")
            setResult(RESULT_OK)
            finish()
        }
    }

    // -----------------------------
    // REQUIRED FOR LANGUAGE SWITCHING
    // -----------------------------
    private fun setLocale(language: String) {
        val locale = java.util.Locale(language)
        java.util.Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
