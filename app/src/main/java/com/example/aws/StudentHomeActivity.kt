package com.example.aws

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StudentHomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.student_home_page)


        val nameText = findViewById<TextView>(R.id.student_name)
        val idText = findViewById<TextView>(R.id.student_id)
        val gpaText = findViewById<TextView>(R.id.student_gpa)
        val departmentText = findViewById<TextView>(R.id.student_department)
        val settingsButton = findViewById<Button>(R.id.settings_button)
        val coursesButton = findViewById<Button>(R.id.courses_button) // if you add it


        val firstName = intent.getStringExtra("first_name")
        val lastName = intent.getStringExtra("last_name")
        val studentId = intent.getStringExtra("student_id")
        val gpa = intent.getStringExtra("gpa")
        val major = intent.getStringExtra("major")


        nameText.text = "Name: $firstName $lastName"
        idText.text = "ID: $studentId"
        gpaText.text = "GPA: $gpa"
        departmentText.text = "Department: $major"


        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("student_id", studentId)
            startActivityForResult(intent, 1)
        }


        coursesButton?.setOnClickListener {
            val intent = Intent(this, CoursesActivity::class.java)
            intent.putExtra("student_id", studentId)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            recreate()
        }
    }
}
