package com.example.aws

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.aws.activities.InstructorClassesActivity

class InstructorHomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.instructor_home_page)

        val nameText = findViewById<TextView>(R.id.instructor_name)
        val idText = findViewById<TextView>(R.id.instructor_id)
        val departmentText = findViewById<TextView>(R.id.instructor_department)
        val settingsButton = findViewById<Button>(R.id.settings_button)
        val myClassesBtn = findViewById<Button>(R.id.courses_button)

        // Get data passed from LoginActivity
        val instructorId = intent.getStringExtra("instructor_id")
        val firstName = intent.getStringExtra("first_name")
        val lastName = intent.getStringExtra("last_name")
        val department = intent.getStringExtra("department")

        // Display data
        nameText.text = "Name: $firstName $lastName"
        idText.text = "ID: $instructorId"
        departmentText.text = "Department: $department"

        // Go to classes page
        myClassesBtn.setOnClickListener {
            val intent = Intent(this, InstructorClassesActivity::class.java)
            intent.putExtra("instructor_id", instructorId)
            startActivity(intent)
        }

        // Settings button
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            recreate()
        }
    }
}
