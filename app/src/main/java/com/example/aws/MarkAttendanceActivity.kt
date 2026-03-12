package com.example.aws

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

class MarkAttendanceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mark_attendance_page)

        val sectionId = intent.getStringExtra("section_id")

        // Find views
        val codeInput = findViewById<EditText>(R.id.codeInput)
        val submitBtn = findViewById<Button>(R.id.submitBtn)

        findViewById<View>(R.id.backButton).setOnClickListener { finish() }

        submitBtn.setOnClickListener {
            val code = codeInput.text.toString().trim()

            if (code.isEmpty()) {
                Toast.makeText(this, "Please enter the code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            verifyCode(sectionId, code)
        }
    }

    private fun verifyCode(sectionId: String?, code: String) {
        // Temporary logic until backend is ready
        Toast.makeText(this, "Code submitted: $code", Toast.LENGTH_SHORT).show()
    }
}
