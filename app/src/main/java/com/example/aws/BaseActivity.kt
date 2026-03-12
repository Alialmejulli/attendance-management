package com.example.aws
import android.os.Bundle
import android.util.Log
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity



open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLocale()
        applyNotificationSettings()
    }

    private fun loadLocale() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val language = prefs.getString("app_language", "en")
        setLocale(language!!)
    }

    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun applyNotificationSettings() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)

        // You can use this to control behavior across the app
        Log.d("BaseActivity", "Notifications enabled: $notificationsEnabled")
    }
}
