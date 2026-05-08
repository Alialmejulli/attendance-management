package com.example.aws

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_page)

        // ── Back button ───────────────────────────────────────────
        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        // ── Profile section ───────────────────────────────────────
        // Pull name and role from SharedPreferences saved at login
        val prefs      = getSharedPreferences("app_settings", MODE_PRIVATE)
        val userPrefs  = getSharedPreferences("user_session", MODE_PRIVATE)

        // Try to get name from intent extras first, fall back to prefs
        val firstName  = intent.getStringExtra("first_name")
            ?: userPrefs.getString("first_name", "") ?: ""
        val lastName   = intent.getStringExtra("last_name")
            ?: userPrefs.getString("last_name", "") ?: ""
        val role       = intent.getStringExtra("role")
            ?: userPrefs.getString("role", "User") ?: "User"

        val fullName   = "$firstName $lastName".trim().ifEmpty { "User" }
        val initial    = firstName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
        val roleLabel  = role.replaceFirstChar { it.uppercaseChar() }

        findViewById<TextView>(R.id.profile_name).text   = fullName
        findViewById<TextView>(R.id.profile_role).text   = roleLabel
        findViewById<TextView>(R.id.profile_avatar).text = initial

        // ── Notifications ─────────────────────────────────────────
        val notificationsSwitch = findViewById<SwitchCompat>(R.id.notifications_switch)
        notificationsSwitch.isChecked = prefs.getBoolean("notifications_enabled", true)
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("notifications_enabled", isChecked) }
        }

        // ── Language buttons ──────────────────────────────────────
        val btnEnglish = findViewById<android.widget.Button>(R.id.btn_english)
        val btnArabic  = findViewById<android.widget.Button>(R.id.btn_arabic)
        val savedLang  = prefs.getString("app_language", "en")

        updateLanguageButtons(btnEnglish, btnArabic, savedLang ?: "en")

        btnEnglish.setOnClickListener {
            prefs.edit { putString("app_language", "en") }
            updateLanguageButtons(btnEnglish, btnArabic, "en")
            setLocale("en")
            setResult(RESULT_OK)
            finish()
        }

        btnArabic.setOnClickListener {
            prefs.edit { putString("app_language", "ar") }
            updateLanguageButtons(btnEnglish, btnArabic, "ar")
            setLocale("ar")
            setResult(RESULT_OK)
            finish()
        }

        // ── About AMS dialog ──────────────────────────────────────
        findViewById<LinearLayout>(R.id.about_app).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("About AMS")
                .setMessage(
                    "Attendance Management System (AMS)\n" +
                            "Version 1.0\n\n" +
                            "AMS is a mobile attendance tracking system developed for King Abdulaziz University (KAU). " +
                            "It streamlines the attendance process by allowing instructors to generate timed session codes " +
                            "that students scan or enter in the app to mark their presence — eliminating paper-based " +
                            "roll calls and manual record keeping.\n\n" +
                            "Key features:\n" +
                            "• Timed session codes with automatic expiry\n" +
                            "• Automatic present / late / absent classification\n" +
                            "• Real-time attendance history for both students and instructors\n" +
                            "• Arabic and English language support\n\n" +
                            "Faculty of Computing & Information Technology\n" +
                            "King Abdulaziz University · Rabigh, Saudi Arabia"
                )
                .setPositiveButton("Close", null)
                .show()
        }

        // ── Logout ────────────────────────────────────────────────
        findViewById<MaterialButton>(R.id.logout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    getSharedPreferences("app_settings",  MODE_PRIVATE).edit().clear().apply()
                    getSharedPreferences("user_session",  MODE_PRIVATE).edit().clear().apply()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    private fun updateLanguageButtons(
        btnEn: android.widget.Button,
        btnAr: android.widget.Button,
        selected: String
    ) {
        if (selected == "ar") {
            btnAr.setBackgroundResource(R.drawable.language_selected)
            btnAr.setTextColor(android.graphics.Color.parseColor("#2E4F3D"))
            btnEn.setBackgroundResource(R.drawable.language_unselected)
            btnEn.setTextColor(android.graphics.Color.WHITE)
        } else {
            btnEn.setBackgroundResource(R.drawable.language_selected)
            btnEn.setTextColor(android.graphics.Color.parseColor("#2E4F3D"))
            btnAr.setBackgroundResource(R.drawable.language_unselected)
            btnAr.setTextColor(android.graphics.Color.WHITE)
        }
    }

    private fun setLocale(language: String) {
        val locale = java.util.Locale(language)
        java.util.Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}